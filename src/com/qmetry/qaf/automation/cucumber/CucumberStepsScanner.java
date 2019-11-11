/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.step.TestStep;

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
import io.cucumber.core.options.CucumberProperties;
import io.cucumber.core.options.CucumberPropertiesParser;
import io.cucumber.core.options.RuntimeOptions;
import io.cucumber.core.runtime.BackendServiceLoader;
import io.cucumber.core.runtime.BackendSupplier;
import io.cucumber.core.runtime.ObjectFactoryServiceLoader;
import io.cucumber.core.runtime.ObjectFactorySupplier;
import io.cucumber.core.runtime.ThreadLocalObjectFactorySupplier;

/**
 * 
 * @author chirag.jayswal
 *
 */
public class CucumberStepsScanner implements Glue {

	public void loadSteps(List<URI> uris) {
		Class<?> clazz = CucumberStepsScanner.class;
		// ClassLoader classLoader = clazz.getClassLoader();

		RuntimeOptions runtimeOptions = new CucumberPropertiesParser().parse(CucumberProperties.fromSystemProperties())
				//.addDefaultSummaryPrinterIfAbsent()
				.build();
		ObjectFactoryServiceLoader objectFactoryServiceLoader = new ObjectFactoryServiceLoader(runtimeOptions);
		ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(objectFactoryServiceLoader);
		BackendSupplier backendSupplier = new BackendServiceLoader(clazz::getClassLoader, objectFactorySupplier);
		Collection<? extends Backend> backends = backendSupplier.get();
		for (Backend backend : backends) {
			//System.out.println("Loading glue for backend " + backend.getClass().getName());
			backend.loadGlue(this, uris);
			objectFactorySupplier.get().start();
			backend.buildWorld();
		}
	}

	@Override
	public void addStepDefinition(StepDefinition stepDefinition) {

		CucumberStep cucumberStep = new CucumberStep(stepDefinition);
		TestStep oldStep = ConfigurationManager.getStepMapping().put(cucumberStep.getName().toUpperCase(),
				cucumberStep);
		if (oldStep != null) {

			// ensure the priority specified while providing step provider
			// package. If list of packages provided, last package has highest
			// priority.
		}
	}

	@Override
	public void addBeforeHook(HookDefinition beforeHook) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAfterHook(HookDefinition afterHook) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addBeforeStepHook(HookDefinition beforeStepHook) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addAfterStepHook(HookDefinition afterStepHook) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addParameterType(ParameterTypeDefinition parameterTypeDefinition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDataTableType(DataTableTypeDefinition dataTableTypeDefinition) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDefaultParameterTransformer(DefaultParameterTransformerDefinition defaultParameterTransformer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDefaultDataTableEntryTransformer(
			DefaultDataTableEntryTransformerDefinition defaultDataTableEntryTransformer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDefaultDataTableCellTransformer(
			DefaultDataTableCellTransformerDefinition defaultDataTableCellTransformer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addDocStringType(DocStringTypeDefinition docStringTypeDefinition) {
		// TODO Auto-generated method stub

	}
	
	public static void main(String[] args) {
		String pkg = "io.cucumber";

		CucumberStepsScanner scanner = new CucumberStepsScanner();
		scanner.loadSteps(Arrays.asList(GluePath.parse(pkg)));
	}

}
