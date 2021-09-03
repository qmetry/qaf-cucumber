package com.qmetry.qaf.automation.cucumber.runner;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static java.util.Collections.max;
import static java.util.Collections.min;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.exec.util.MapUtils;
import org.jsoup.internal.StringUtil;
import org.testng.ITestContext;
import org.testng.annotations.Factory;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.cucumber.QAFCucumberPlugin;
import com.qmetry.qaf.automation.keys.ApplicationProperties;
import com.qmetry.qaf.automation.util.DateUtil;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.feature.FeatureParser;
import io.cucumber.core.filter.Filters;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.plugin.PluginFactory;
import io.cucumber.core.plugin.Plugins;
import io.cucumber.core.resource.ClassLoaders;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.FeaturePathFeatureSupplier;
import io.cucumber.core.runtime.FeatureSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.RunnerSupplier;
import io.cucumber.core.runtime.ScanningTypeRegistryConfigurerSupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalRunnerSupplier;
import io.cucumber.core.runtime.TimeServiceEventBus;
import io.cucumber.core.runtime.TypeRegistryConfigurerSupplier;
import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestSourceRead;

public class CucumberScenarioFactory {

	@Factory
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object[] getTestsFromFile(ITestContext context) {
		ConfigurationManager.addAll(context.getCurrentXmlTest().getAllParameters());
		context.getCurrentXmlTest().getLocalParameters().put("testname", context.getCurrentXmlTest().getName());
		getBundle().setProperty("testname", context.getCurrentXmlTest().getName());
		
		EventBus eventBus = new TimeServiceEventBus(Clock.systemUTC(), UUID::randomUUID);
		context.setAttribute("eventBus", eventBus);
		if(!getBundle().containsKey("eventBus")) {
			getBundle().setProperty("eventBus", new ArrayList<EventBus>());
		}
		((List<EventBus>)getBundle().getProperty("eventBus")).add(eventBus);

		getBundle().setProperty("suite.name", context.getCurrentXmlTest().getSuite().getName());
		if (StringUtil.isBlank(ApplicationProperties.JSON_REPORT_DIR.getStringVal(""))) {
			String dir = ApplicationProperties.JSON_REPORT_ROOT_DIR.getStringVal("test-results") + "/"
					+ DateUtil.getDate(0, "EdMMMyy_hhmmssa");
			getBundle().setProperty(ApplicationProperties.JSON_REPORT_DIR.key, dir);
		}
		Properties source = ConfigurationConverter.getProperties(getBundle().subset("cucumber"));
		Map props = new HashMap(MapUtils.prefix(source, "cucumber"));
		RuntimeOptions runtimeOptions = new CucumberPropertiesParser().parse(props).build();

		Supplier<ClassLoader> classLoader = ClassLoaders::getDefaultClassLoader;

		final ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
		ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
		final BackendSupplier backendSupplier = new BackendServiceLoader(classLoader, objectFactorySupplier);
		final Plugins plugins = new Plugins(new PluginFactory(), runtimeOptions);
		final ExitStatus exitStatus = new ExitStatus(runtimeOptions);
		plugins.addPlugin(exitStatus);
		QAFCucumberPlugin qafCucumberPlugin = new QAFCucumberPlugin();
		if (plugins.getPlugins().stream().noneMatch(p -> (p instanceof QAFCucumberPlugin))) {
			plugins.addPlugin(qafCucumberPlugin);
			System.out.println("Added QAFCucumberPlugin");
		}

		plugins.setSerialEventBusOnEventListenerPlugins(eventBus);
		final TypeRegistryConfigurerSupplier typeRegistryConfigurerSupplier = new ScanningTypeRegistryConfigurerSupplier(
				classLoader, runtimeOptions);
		final RunnerSupplier runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, eventBus, backendSupplier,
				objectFactorySupplier, typeRegistryConfigurerSupplier);
		final FeatureParser parser = new FeatureParser(eventBus::generateId);
		final FeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(classLoader, runtimeOptions, parser);

		final Predicate<Pickle> filter = new Filters(runtimeOptions);

		final List<Feature> features = featureSupplier.get();

		for (Feature feature : features) {
			eventBus.send(new TestSourceRead(eventBus.getInstant(), feature.getUri(), feature.getSource()));
		}

		final List<CucumberScenario> cucumberScenarios = features.stream()
				.flatMap(feature -> feature.getPickles().stream()).filter(filter)
				.map(pickle -> new CucumberScenario(pickle.getName(), pickle, runnerSupplier.get())).collect(toList());

		return cucumberScenarios.toArray();
	}

	static final class ExitStatus implements ConcurrentEventListener {
		private static final byte DEFAULT = 0x0;
		private static final byte ERRORS = 0x1;

		private final List<Result> results = new ArrayList<>();
		private final RuntimeOptions runtimeOptions;

		private final EventHandler<TestCaseFinished> testCaseFinishedHandler = event -> results.add(event.getResult());

		ExitStatus(RuntimeOptions runtimeOptions) {
			this.runtimeOptions = runtimeOptions;
		}

		@Override
		public void setEventPublisher(EventPublisher publisher) {
			publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
		}

		byte exitStatus() {
			if (results.isEmpty()) {
				return DEFAULT;
			}

			if (runtimeOptions.isWip()) {
				Result leastSeverResult = min(results, comparing(Result::getStatus));
				return leastSeverResult.getStatus().is(Status.PASSED) ? ERRORS : DEFAULT;
			} else {
				Result mostSevereResult = max(results, comparing(Result::getStatus));
				return mostSevereResult.getStatus().isOk(runtimeOptions.isStrict()) ? DEFAULT : ERRORS;
			}
		}
	}

}
