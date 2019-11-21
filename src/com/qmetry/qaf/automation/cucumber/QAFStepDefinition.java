/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.util.List;

import com.qmetry.qaf.automation.step.TestStep;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;

/**
 * @author chirag.jayswal
 *
 */
public class QAFStepDefinition implements StepDefinition {
	TestStep step;
	Lookup lookup;
	public QAFStepDefinition(TestStep step, Lookup lookup) {
		this.step = step;
		this.lookup = lookup;
	}
	/* (non-Javadoc)
	 * @see io.cucumber.core.backend.Located#isDefinedAt(java.lang.StackTraceElement)
	 */
	@Override
	public boolean isDefinedAt(StackTraceElement stackTraceElement) {
        //return e.getClassName().equals(method.getDeclaringClass().getName()) && e.getMethodName().equals(method.getName());
		return false;
	}

	/* (non-Javadoc)
	 * @see io.cucumber.core.backend.Located#getLocation()
	 */
	@Override
	public String getLocation() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see io.cucumber.core.backend.StepDefinition#execute(java.lang.Object[])
	 */
	@Override
	public void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException {
		step.setActualArgs(args);
		step.execute();
	}

	/* (non-Javadoc)
	 * @see io.cucumber.core.backend.StepDefinition#parameterInfos()
	 */
	@Override
	public List<ParameterInfo> parameterInfos() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see io.cucumber.core.backend.StepDefinition#getPattern()
	 */
	@Override
	public String getPattern() {
		return step.getDescription();
	}

}
