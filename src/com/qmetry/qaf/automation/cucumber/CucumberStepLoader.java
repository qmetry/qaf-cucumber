/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static com.qmetry.qaf.automation.cucumber.CucumberStepsFinder.buildBackendWorlds;
import static com.qmetry.qaf.automation.cucumber.CucumberStepsFinder.disposeBackendWorlds;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * 
 * @author chirag.jayswal
 *
 */
public class CucumberStepLoader implements ITestListener {

	@Override
	public void onStart(ITestContext context) {
	}

	@Override
	public void onFinish(ITestContext context) {
	}

	@Override
	public void onTestStart(ITestResult result) {
		if (!getBundle().getBoolean("cucumber.run.mode", false)) {
			buildBackendWorlds();
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		testCaseCompleted();
	}

	@Override
	public void onTestFailure(ITestResult result) {
		testCaseCompleted();
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		testCaseCompleted();
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		testCaseCompleted();
	}

	private void testCaseCompleted() {
		if (!getBundle().getBoolean("cucumber.run.mode", false)) {
			disposeBackendWorlds();
		}
	}

}
