/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.step.JavaStep;
import com.qmetry.qaf.automation.step.TestStep;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;

/**
 * @author chirag.jayswal
 *
 */
public class QAFBackend implements Backend {

	private final Lookup lookup;
	private final Container container;
	//private final ClassFinder classFinder;

	QAFBackend(Lookup lookup, Container container, Supplier<ClassLoader> classLoaderSupplier) {
		this.lookup = lookup;
		this.container = container;
		//ClassLoader classLoader = classLoaderSupplier.get();
		//MultiLoader resourceLoader = new MultiLoader(classLoader);
		//this.classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.Backend#loadGlue(io.cucumber.core.backend.Glue,
	 * java.util.List)
	 */
	@Override
	public void loadGlue(Glue glue, List<URI> gluePaths) {
		Map<String, TestStep> steps = (Map<String, TestStep>) ConfigurationManager.getStepMapping();
		for (TestStep step : steps.values()) {
			glue.addStepDefinition(new QAFStepDefinition(step, lookup));
			if (step instanceof JavaStep) {
				container.addClass(((JavaStep) step).getMethod().getDeclaringClass());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.Backend#buildWorld()
	 */
	@Override
	public void buildWorld() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.Backend#disposeWorld()
	 */
	@Override
	public void disposeWorld() {

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.Backend#getSnippet()
	 */
	@Override
	public Snippet getSnippet() {
		return null;
	}

}
