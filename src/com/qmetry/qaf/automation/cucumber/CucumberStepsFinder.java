/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static com.qmetry.qaf.automation.core.ConfigurationManager.getStepMapping;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.qmetry.qaf.automation.core.TestBaseProvider;
import com.qmetry.qaf.automation.keys.ApplicationProperties;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ParameterTypeDefinition;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.feature.GluePath;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;

/**
 * 
 * @author chirag.jayswal
 *
 */
public class CucumberStepsFinder implements Glue {
	private static final String CUCUMBER_BACKENDS = "cucumber.runtime.backend";
	private static final String CUCUMBER_GLUE = "cucumber.runtime.glue";

	private static final ObjectFactoryServiceLoader serviceLoader = new ObjectFactoryServiceLoader(
			RuntimeOptions.defaultOptions());
	private static final ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(
			serviceLoader);

	@Override
	public void addStepDefinition(StepDefinition stepDefinition) {
		CucumberStep cucumberStep = new CucumberStep(stepDefinition);
		getStepMapping().put(cucumberStep.getName().toUpperCase(), cucumberStep);
	}

	@Override
	public void addBeforeHook(HookDefinition beforeHook) {
	}

	@Override
	public void addAfterHook(HookDefinition afterHook) {
	}

	@Override
	public void addBeforeStepHook(HookDefinition beforeStepHook) {
	}

	@Override
	public void addAfterStepHook(HookDefinition afterStepHook) {
	}

	@Override
	public void addParameterType(ParameterTypeDefinition parameterTypeDefinition) {
	}

	@Override
	public void addDataTableType(DataTableTypeDefinition dataTableTypeDefinition) {
	}

	@Override
	public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
	}

	@Override
	public void addDefaultDataTableEntryTransformer(
			DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
	}

	@Override
	public void addDefaultDataTableCellTransformer(
			DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {

	}

	@Override
	public void addDocStringType(DocStringTypeDefinition docStringTypeDefinition) {

	}


	public static void buildBackendWorlds() {
		try {
			Collection<? extends Backend> backends = getBackends();
			objectFactorySupplier.get().start();
			//Lambda expressions get loaded during buildWorld instead of loadGlue method
			for (Backend backend : backends) {
				backend.buildWorld();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void disposeBackendWorlds() {
		try {
			Collection<? extends Backend> backends = getBackends();
			for (Backend backend : backends) {
				backend.disposeWorld();
			}
			objectFactorySupplier.get().stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	private static Collection<? extends Backend> getBackends() {
		Collection<? extends Backend> backends = (Collection<? extends Backend>) TestBaseProvider.instance().get()
				.getContext().getObject(CUCUMBER_BACKENDS);
		if (null == backends) {
			backends = new BackendServiceLoader(() -> Thread.currentThread().getContextClassLoader(),
					objectFactorySupplier).get().stream().filter(b->!(b instanceof QAFBackend)).collect(Collectors.toList());
	
			TestBaseProvider.instance().get().getContext().setProperty(CUCUMBER_BACKENDS, backends);
			CucumberStepsFinder glue = new CucumberStepsFinder();
			TestBaseProvider.instance().get().getContext().setProperty(CUCUMBER_GLUE, glue);

			List<URI> uris = new ArrayList<URI>();
			for (String pkg : getBundle().getStringArray(ApplicationProperties.STEP_PROVIDER_PKG.key)) {
				uris.add(GluePath.parse(pkg));
			}
			for (Backend backend : getBackends()) {
				backend.loadGlue(glue, uris);
			}
		}
		return backends;
	}
}
