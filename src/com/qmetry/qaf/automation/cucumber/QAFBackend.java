/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import static java.util.stream.Collectors.joining;

import java.lang.reflect.Type;
import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.step.JavaStep;
import com.qmetry.qaf.automation.step.NotYetImplementedException;
import com.qmetry.qaf.automation.step.TestStep;

import io.cucumber.core.backend.Backend;
import io.cucumber.core.backend.Container;
import io.cucumber.core.backend.Glue;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.Snippet;
import io.cucumber.datatable.DataTable;

/**
 * @author chirag
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
		return new QAFSnippet();
	}
	
	class QAFSnippet  implements Snippet {

	    @Override
	    public MessageFormat template() {
	        return new MessageFormat("" +
	            "@QAFTestStep(\"{1}\")\n" +
	            "public void {2}({3}) '{'\n" +
	            "    // {4}\n" +
	            "{5}    throw new " + NotYetImplementedException.class.getName() + "();\n" +
	            "'}'");
	    }

		@Override
		public String tableHint() {
			return "";
		}

		@Override
		public String arguments(Map<String, Type> arguments) {
			return arguments.entrySet()
		            .stream()
		            .map(argType -> getArgType(argType.getValue()) + " " + argType.getKey())
		            .collect(joining(", "));
		}

	    private String getArgType(Type argType) {
	        if (argType instanceof Class) {
	            Class cType = (Class) argType;
	            if (cType.equals(DataTable.class)) {
	                return cType.getName();
	            }
	            return cType.getSimpleName();
	        }

	        // Got a better idea? Send a PR.
	        return argType.toString();
	    }
		@Override
		public String escapePattern(String pattern) {
	        return pattern.replace("\\", "\\\\").replace("\"", "\\\"");
		}
	}

}
