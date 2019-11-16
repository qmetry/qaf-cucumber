/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.keys.ApplicationProperties;
import com.qmetry.qaf.automation.step.StepFinder;
import com.qmetry.qaf.automation.step.TestStep;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.DataTableTypeDefinition;
import io.cucumber.core.backend.DefaultDataTableCellTransformerDefinition;
import io.cucumber.core.backend.DefaultDataTableEntryTransformerDefinition;
import io.cucumber.core.backend.DefaultParameterTransformerDefinition;
import io.cucumber.core.backend.DocStringTypeDefinition;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.HookDefinition;
import io.cucumber.core.backend.ObjectFactory;
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
public class CucumberStepsFinder implements Glue, StepFinder {

	private static final ObjectFactoryServiceLoader serviceLoader = new ObjectFactoryServiceLoader(
			RuntimeOptions.defaultOptions());
	private static final ObjectFactorySupplier objectFactorySupplier = new ThreadLocalObjectFactorySupplier(
			serviceLoader);
	private static final ObjectFactory objectFactory = objectFactorySupplier.get();

	private final static Collection<? extends Backend> backends = new BackendServiceLoader(
			() -> Thread.currentThread().getContextClassLoader(), objectFactorySupplier).get();
	private Set<TestStep> steps = new HashSet<>();
	private boolean scanningMode = false;

	@Override
	public void addStepDefinition(StepDefinition stepDefinition) {

		CucumberStep cucumberStep = new CucumberStep(stepDefinition);
		steps.add(cucumberStep);
		if (!scanningMode) {
			//Lambda expressions get loaded during buildWorld instead of loadGlue method
			ConfigurationManager.getStepMapping().put(cucumberStep.getName().toUpperCase(), cucumberStep);
		}
	}

	@Override
	public void addBeforeHook(HookDefinition beforeHook) {
		// TODO Auto-generated method stub

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

	@Override
	public Set<TestStep> getAllJavaSteps(List<String> pkgs) {
		scanningMode = true;
		
		steps.clear();
		List<URI> uris = new ArrayList<URI>();
		for (String pkg : ConfigurationManager.getBundle()
				.getStringArray(ApplicationProperties.STEP_PROVIDER_PKG.key)) {
			uris.add(GluePath.parse(pkg));
		}
		for (Backend backend : backends) {
			backend.loadGlue(this, uris);
		}
		scanningMode = false;
		return steps;
	}

	public static void buildBackendWorlds() {
		try {
			objectFactory.start();
			for (Backend backend : backends) {
				backend.buildWorld();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void disposeBackendWorlds() {
		try {
			for (Backend backend : backends) {
				backend.disposeWorld();
			}
			objectFactory.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
