package com.qmetry.qaf.automation.cucumber.runner;

import static com.qmetry.qaf.automation.cucumber.QAFCucumberPlugin.getBdd2Pickle;
import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import java.util.Collection;
import java.util.stream.Collectors;

import org.testng.ITestContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.qmetry.qaf.automation.core.AutomationError;
import com.qmetry.qaf.automation.cucumber.bdd2.model.BDD2PickleWrapper;
import com.qmetry.qaf.automation.step.StringTestStep;
import com.qmetry.qaf.automation.step.TestStep;
import com.qmetry.qaf.automation.step.client.Scenario;

import io.cucumber.core.eventbus.EventBus;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.runner.Runner;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestRunFinished;
public class CucumberScenario extends Scenario {

	private BDD2PickleWrapper bdd2pickle;
	private Runner runner;
    private final EventHandler<TestCaseFinished> testCaseFinished = this::handleTestCaseFinished;
    private Result result;
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
		afterScenario();
		
	}


	@Override
	protected void beforeScanario() {
		super.beforeScanario();
		runner.getBus().registerHandlerFor(TestCaseFinished.class, testCaseFinished);
	}
	
	private void afterScenario() {
        runner.getBus().removeHandlerFor(TestCaseFinished.class, testCaseFinished);
        Status status = result.getStatus();
        if (!status.is(Status.PASSED)) {
        	Throwable error = result.getError();
        	try {
        	 if (status.is(Status.SKIPPED) && error != null) {
                throw new AutomationError(error);
            } else if (status.is(Status.SKIPPED) || status.is(Status.UNDEFINED)) {
                throw new AutomationError(status.name());

            } else if (status.is(Status.PENDING)) {
                throw new AutomationError(status.name(),error);
            }
        	 if(null==error) {
             	throw new RuntimeException("Error=null while with status=" + result.getStatus());
             }
            throw error;
        	}catch(RuntimeException t) {
        		throw (RuntimeException)t;
        	}catch (Error r) {
        		throw (Error)r;
        	}catch(Throwable t) {
        		new RuntimeException(error);
        	}
        }
	}
	
	private void handleTestCaseFinished(TestCaseFinished event) {
        result = event.getResult();
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

	@Override
	public Collection<TestStep> getSteps() {
		return bdd2pickle.getSteps().stream().map(s->new StringTestStep(s.getText())).collect(Collectors.toList());
	}
}
