/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.qmetry.qaf.automation.step.JavaStep;
import com.qmetry.qaf.automation.step.TestStep;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TypeResolver;

/**
 * @author chirag
 *
 */
public class QAFStepDefinition implements StepDefinition {
	TestStep step;
	Lookup lookup;

	public QAFStepDefinition(TestStep step, Lookup lookup) {
		this.step = step;
		this.lookup = lookup;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.cucumber.core.backend.Located#isDefinedAt(java.lang.StackTraceElement)
	 */
	@Override
	public boolean isDefinedAt(StackTraceElement stackTraceElement) {
		// return e.getClassName().equals(method.getDeclaringClass().getName()) &&
		// e.getMethodName().equals(method.getName());
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.Located#getLocation()
	 */
	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.StepDefinition#execute(java.lang.Object[])
	 */
	@Override
	public void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException {
		step.setActualArgs(args);
		step.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.StepDefinition#parameterInfos()
	 */
	@Override
	public List<ParameterInfo> parameterInfos() {
		List<ParameterInfo> parameterInfos = new ArrayList<ParameterInfo>();
		if (step instanceof JavaStep) {
			for (Type type : ((JavaStep) step).getMethod().getGenericParameterTypes()) {
				parameterInfos.add(new ParameterInfoImpl(type));
			}
		}
		return parameterInfos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.StepDefinition#getPattern()
	 */
	@Override
	public String getPattern() {

		return step.getDescription().replaceAll("\\{[a-zA-Z0-9_-]+\\}", "{string}");
	}

	class ParameterInfoImpl implements ParameterInfo {

		Type type;

		public ParameterInfoImpl(Type type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			return type;
		}

		@Override
		public boolean isTransposed() {
			return false;
		}

		@Override
		public TypeResolver getTypeResolver() {
			return () -> type;
		}

	}

	public static void main(String[] args) {
		String s="a is {task-name} and {threshold}";
		System.out.println(s.replaceAll("\\{[a-zA-Z0-9_-]+\\}", "{string}"));
	}
}
