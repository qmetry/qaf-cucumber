/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static com.qmetry.qaf.automation.cucumber.CucumberStepsFinder.buildBackendWorlds;
import static com.qmetry.qaf.automation.cucumber.CucumberStepsFinder.disposeBackendWorlds;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

/**
 * This class will be used when QAF BDD factory is used as runner.
 * 
 * @author chirag.jayswal
 *
 */
public class CucumberStepLoader implements IInvokedMethodListener {

	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.isTestMethod() && !getBundle().getBoolean("cucumber.run.mode", false)) {
			buildBackendWorlds();
		}
	}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
		if (method.isTestMethod() && !getBundle().getBoolean("cucumber.run.mode", false)) {
			disposeBackendWorlds();
		}
	}
}
