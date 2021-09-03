package com.qmetry.qaf.automation.cucumber.runner;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static com.qmetry.qaf.automation.cucumber.QAFCucumberPlugin.getBdd2Pickle;

import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.qmetry.qaf.automation.cucumber.bdd2.model.BDD2PickleWrapper;
import com.qmetry.qaf.automation.step.client.Scenario;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runner.Runner;
import io.cucumber.plugin.event.TestRunFinished;

public class CucumberScenario extends Scenario {

	private BDD2PickleWrapper bdd2pickle;
	private Runner runner;

	public CucumberScenario(String testName, Pickle pickle, Runner runner) {
		super(testName, null, getBdd2Pickle(pickle).getMetaData());
		bdd2pickle = getBdd2Pickle(pickle);
		this.runner = runner;
		bdd2pickle.setMetaData(getMetadata());
	}

	@Test(groups = "scenario")
	public void scenario() {
		beforeScanario();
		runner.runPickle(bdd2pickle);
	}

	@BeforeTest(alwaysRun = true)
	public void setTestName(ITestContext context) {
		getBundle().setProperty("usingtestngrunner", true);
		getBundle().setProperty("testname", context.getCurrentXmlTest().getName());
	}

	@AfterTest(alwaysRun = true)
	public void testRunFinished(ITestContext context) {
		EventBus eventBus = (EventBus) context.getAttribute("eventBus");
		eventBus.send(new TestRunFinished(eventBus.getInstant()));
	}

}
