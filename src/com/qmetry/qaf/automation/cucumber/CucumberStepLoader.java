/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import com.qmetry.qaf.automation.core.ConfigurationManager;
import com.qmetry.qaf.automation.keys.ApplicationProperties;

import io.cucumber.core.feature.GluePath;

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
		List<URI> uris = new ArrayList<URI>();
		for (String pkg : ConfigurationManager.getBundle()
				.getStringArray(ApplicationProperties.STEP_PROVIDER_PKG.key)) {
			uris.add(GluePath.parse(pkg));
		}

		try {
			new CucumberStepsScanner().loadSteps(uris);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onTestSuccess(ITestResult result) {
	}

	@Override
	public void onTestFailure(ITestResult result) {
	}

	@Override
	public void onTestSkipped(ITestResult result) {
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
	}

}
