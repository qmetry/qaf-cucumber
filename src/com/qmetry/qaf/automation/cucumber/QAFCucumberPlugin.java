/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static com.qmetry.qaf.automation.data.MetaDataScanner.applyMetaRule;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.configuration.ConfigurationConverter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;

import com.qmetry.qaf.automation.core.AutomationError;
import com.qmetry.qaf.automation.core.CheckpointResultBean;
import com.qmetry.qaf.automation.core.LoggingBean;
import com.qmetry.qaf.automation.core.MessageTypes;
import com.qmetry.qaf.automation.core.QAFTestBase;
import com.qmetry.qaf.automation.core.TestBaseProvider;
import com.qmetry.qaf.automation.cucumber.bdd2.model.BDD2PickleWrapper;
import com.qmetry.qaf.automation.integration.ResultUpdator;
import com.qmetry.qaf.automation.integration.TestCaseResultUpdator;
import com.qmetry.qaf.automation.integration.TestCaseRunResult;
import com.qmetry.qaf.automation.util.ClassUtil;
import com.qmetry.qaf.automation.util.Reporter;
import com.qmetry.qaf.automation.util.StringMatcher;
import com.qmetry.qaf.automation.util.StringUtil;

import io.cucumber.plugin.ConcurrentEventListener;
import io.cucumber.plugin.event.EmbedEvent;
import io.cucumber.plugin.event.EventHandler;
import io.cucumber.plugin.event.EventPublisher;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.cucumber.plugin.event.Result;
import io.cucumber.plugin.event.Status;
import io.cucumber.plugin.event.TestCase;
import io.cucumber.plugin.event.TestCaseFinished;
import io.cucumber.plugin.event.TestCaseStarted;
import io.cucumber.plugin.event.TestRunFinished;
import io.cucumber.plugin.event.TestRunStarted;
import io.cucumber.plugin.event.TestStepFinished;
import io.cucumber.plugin.event.TestStepStarted;
import io.cucumber.plugin.event.WriteEvent;

/**
 * This is cucumber plugin need to be used when Cucumber runner is used. It will
 * generate QAF JSON reports.
 * 
 * @author chirag.jayswal
 *
 */
public class QAFCucumberPlugin implements ConcurrentEventListener {
	private static final Log logger = LogFactoryImpl.getLog(QAFCucumberPlugin.class);

	
	@Override
	public void setEventPublisher(EventPublisher publisher) {
		setCucumberRunner(true);

		publisher.registerHandlerFor(TestRunStarted.class, runStartedHandler);
		publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);

		publisher.registerHandlerFor(TestCaseStarted.class, tcStartedHandler);
		publisher.registerHandlerFor(TestCaseFinished.class, tcfinishedHandler);

		publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
		publisher.registerHandlerFor(TestStepFinished.class, stepfinishedHandler);
		publisher.registerHandlerFor(EmbedEvent.class, embedEventHandler);
		publisher.registerHandlerFor(WriteEvent.class, (event) -> {
			Reporter.log(event.getText());
		});

	}

	private EventHandler<EmbedEvent> embedEventHandler = new EventHandler<EmbedEvent>() {

		@Override
		public void receive(EmbedEvent event) {
			// event.
		}
	};

	private EventHandler<TestStepStarted> stepStartedHandler = new EventHandler<TestStepStarted>() {

		@Override
		public void receive(TestStepStarted event) {
			// TestStep step = event.getTestStep();
			QAFTestBase stb = TestBaseProvider.instance().get();
			ArrayList<CheckpointResultBean> allResults = new ArrayList<CheckpointResultBean>(
					stb.getCheckPointResults());
			ArrayList<LoggingBean> allCommands = new ArrayList<LoggingBean>(stb.getLog());
			stb.getCheckPointResults().clear();
			stb.getLog().clear();
			stb.getContext().setProperty("allResults", allResults);
			stb.getContext().setProperty("allCommands", allCommands);

		}
	};
	private EventHandler<TestStepFinished> stepfinishedHandler = new EventHandler<TestStepFinished>() {

		@Override
		public void receive(TestStepFinished event) {
			if (event.getTestStep() instanceof PickleStepTestStep) {
				logStep((PickleStepTestStep) event.getTestStep(), event);
			}
		}

		@SuppressWarnings("unchecked")
		private void logStep(PickleStepTestStep testStep, TestStepFinished event) {
			;
			Result result = event.getResult();
			String stepText = testStep.getStep().getKeyWord() + testStep.getStep().getText();
			Long duration = result.getDuration().toMillis();
			QAFTestBase stb = TestBaseProvider.instance().get();

			if (result.getError() != null) {
				CheckpointResultBean failureCheckpoint = new CheckpointResultBean();
				failureCheckpoint.setMessage(result.getError().getMessage());
				failureCheckpoint.setType(MessageTypes.Fail);
				stb.getCheckPointResults().add(failureCheckpoint);
			}
			if (result.getStatus().is(Status.UNDEFINED)) {
				stepText = stepText + ": Not Found";
				stb.addVerificationError(event.getTestStep().getCodeLocation() + "TestStep implementation not found");
			}

			MessageTypes type = result.getStatus().is(Status.PASSED)
					&& getStepMessageType(stb.getCheckPointResults()).isFailure() ? MessageTypes.TestStepFail
							: getStepMessageType(result.getStatus(), isDryRun(event.getTestCase()));
			// MessageTypes type = success? getStepMessageType(stb.getCheckPointResults()) :
			// MessageTypes.;

			LoggingBean stepLogBean = new LoggingBean(testStep.getPattern(),
					testStep.getDefinitionArgument().stream().map(a -> {
						return a.getValue();
					}).collect(Collectors.toList()).toArray(new String[] {}), result.getStatus().name());
			stepLogBean.setSubLogs(new ArrayList<LoggingBean>(stb.getLog()));

			CheckpointResultBean stepResultBean = new CheckpointResultBean();
			stepResultBean.setMessage(stepText);
			stepResultBean.setSubCheckPoints(new ArrayList<CheckpointResultBean>(stb.getCheckPointResults()));
			stepResultBean.setDuration(duration.intValue());

			stepResultBean.setType(type);

			ArrayList<CheckpointResultBean> allResults = (ArrayList<CheckpointResultBean>) stb.getContext()
					.getObject("allResults");
			ArrayList<LoggingBean> allCommands = (ArrayList<LoggingBean>) stb.getContext().getObject("allCommands");
			stb.getContext().clearProperty("allResults");
			stb.getContext().clearProperty("allCommands");

			allResults.add(stepResultBean);

			stb.getCheckPointResults().clear();
			stb.getCheckPointResults().addAll(allResults);

			allCommands.add(stepLogBean);
			stb.getLog().clear();
			stb.getLog().addAll(allCommands);
		}

		private MessageTypes getStepMessageType(List<CheckpointResultBean> subSteps) {
			MessageTypes type = MessageTypes.TestStepPass;
			for (CheckpointResultBean subStep : subSteps) {
				type = MessageTypes.TestStepPass;
				if (StringMatcher.containsIgnoringCase("fail").match(subStep.getType())) {
					return MessageTypes.TestStepFail;
				}
				if (StringMatcher.containsIgnoringCase("warn").match(subStep.getType())) {
					type = MessageTypes.Warn;
				}
			}
			return type;
		}

		private MessageTypes getStepMessageType(io.cucumber.plugin.event.Status status, boolean isDryRun) {
			switch (status) {
			case PASSED:
				return MessageTypes.TestStepPass;
			case FAILED:
			case UNDEFINED:
				return MessageTypes.TestStepFail;
			case AMBIGUOUS:
				return MessageTypes.Warn;
			default:
				if (isDryRun) {
					return MessageTypes.TestStepPass;
				}
				return MessageTypes.TestStep;
			}
		}
	};

	private EventHandler<TestCaseStarted> tcStartedHandler = new EventHandler<TestCaseStarted>() {

		@Override
		public void receive(TestCaseStarted event) {
			BDD2PickleWrapper bdd2Pickle = getBdd2Pickle(event.getTestCase());
			bdd2Pickle.getMetaData().put("reference", event.getTestCase().getUri());
			QAFTestBase stb = TestBaseProvider.instance().get();
			stb.getLog().clear();
			stb.clearVerificationErrors();
			stb.getCheckPointResults().clear();
		}
	};

	private boolean isDryRun(TestCase tc) {
		return (boolean) getField("dryRun", tc);
	}

	private EventHandler<TestCaseFinished> tcfinishedHandler = new EventHandler<TestCaseFinished>() {

		@Override
		public void receive(TestCaseFinished event) {
			try {
				TestCase tc = event.getTestCase();
				BDD2PickleWrapper bdd2Pickle = getBdd2Pickle(tc);
				boolean isDryRun = isDryRun(tc);
				Result result = event.getResult();

				Throwable throwable = result.getError();
				QAFTestBase stb = TestBaseProvider.instance().get();

				if (isDryRun) {
					Map<String, Object> metadata = new HashMap<String, Object>(bdd2Pickle.getMetaData());
					if (null != bdd2Pickle.getTestData()) {
						metadata.putAll(bdd2Pickle.getTestData());
					}
					String vresult = applyMetaRule(metadata);
					if (StringUtil.isNotBlank(vresult)) {
						throwable = new AutomationError("Metadata rule failure:" + vresult);
						stb.addVerificationError(throwable);
					}
				}
				final List<CheckpointResultBean> checkpoints = new ArrayList<CheckpointResultBean>(
						stb.getCheckPointResults());
				final List<LoggingBean> logs = new ArrayList<LoggingBean>(stb.getLog());

				if (stb.getVerificationErrors() > 0 && (result.getStatus().is(Status.PASSED) || isDryRun)) {

					result = new Result(null != throwable ? result.getStatus() : Status.FAILED, result.getDuration(),
							throwable);
					try {
						ClassUtil.setField("result", event, result);
					} catch (Exception e) {
					}
				} else if (isDryRun && (null == throwable)) {
					result = new Result(Status.PASSED, result.getDuration(), throwable);
					try {
						ClassUtil.setField("result", event, result);
					} catch (Exception e) {
					}
				}
				String className = getClassName(tc.getUri());
//						tc.getScenarioDesignation()
//						.substring(0, tc.getScenarioDesignation().indexOf(".feature")).replaceAll("/", ".");
				QAFReporter.createMethodResult(className, bdd2Pickle, result.getDuration().toMillis(),
						result.getStatus().name(), result.getError(), logs, checkpoints);
				if (!isDryRun) {
					deployResult(bdd2Pickle, tc, result);
				}
				String useSingleSeleniumInstance = getBundle().getString("selenium.singletone", "");
				if (useSingleSeleniumInstance.toUpperCase().startsWith("M")) {
					stb.tearDown();
				}
			} catch (Exception e) {
				logger.error("QAFCucumberPlugin unable to process TestCaseFinished event", e);
			}
		}

		private String getClassName(URI uri) {
			try {
				return FilenameUtils.removeExtension(new File(uri).getName());
			} catch (Exception e) {
				return "allscenarios";
			}
		}
		
		private void deployResult(BDD2PickleWrapper bdd2Pickle, TestCase tc, Result eventresult) {
			String updator = getBundle().getString("result.updator");
			try {
				if (StringUtil.isNotBlank(updator)) {
					TestCaseRunResult.Status result = eventresult.getStatus() == Status.PASSED ? TestCaseRunResult.Status.PASS
							: eventresult.getStatus() == Status.FAILED ? TestCaseRunResult.Status.FAIL
									: TestCaseRunResult.Status.SKIPPED;

					// String method = testCase.getName();
					Class<?> updatorCls = Class.forName(updator);

					TestCaseResultUpdator updatorObj = (TestCaseResultUpdator) updatorCls.newInstance();
					long stTime = System.currentTimeMillis() - eventresult.getDuration().toMillis();

					Map<String, Object> executionInfo = new HashMap<String, Object>();
					executionInfo.put("testName", getBundle().getString("testname", "BDD2"));
					executionInfo.put("suiteName", QAFReporter.SUITENAME);
					executionInfo.put("env", ConfigurationConverter.getMap(getBundle().subset("env")));

					if (null != bdd2Pickle && null != bdd2Pickle.getMetaData()) {
						List<String> steps = bdd2Pickle.getSteps().stream().map(s->s.getText()).collect(Collectors.toList());
						TestCaseRunResult testCaseRunResult = new TestCaseRunResult(result, bdd2Pickle.getMetaData(),
								new Object[] {bdd2Pickle.getTestData()}, executionInfo, steps,stTime);
						ResultUpdator.updateResult(testCaseRunResult,updatorObj);
					}else {
						logger.warn("QAFCucumberPlugin is unable to deploy result");
					}
				}
			} catch (Exception e) {
				logger.warn("QAFCucumberPlugin is unable to deploy result", e);
			}
		}
	};

	private EventHandler<TestRunStarted> runStartedHandler = new EventHandler<TestRunStarted>() {
		@Override
		public void receive(TestRunStarted event) {
			startReport(event);
		}

		private void startReport(TestRunStarted event) {
			QAFReporter.createMetaInfo();
		}
	};
	private EventHandler<TestRunFinished> runFinishedHandler = new EventHandler<TestRunFinished>() {
		@Override
		public void receive(TestRunFinished event) {
			endReport(event);
		}

		private void endReport(TestRunFinished event) {
			QAFReporter.updateMetaInfo();
			QAFReporter.updateOverview(null, true);
			;

			TestBaseProvider.instance().stopAll();
			ResultUpdator.awaitTermination();
		}
	};

	private static BDD2PickleWrapper getBdd2Pickle(Object testCase) {
		try {
			Object pickle = getField("pickle", testCase);
			if (pickle instanceof BDD2PickleWrapper) {
				return ((BDD2PickleWrapper) pickle);
			} else {
				return getBdd2Pickle(pickle);
			}
		} catch (Exception e) {
			return null;
		}
	}

	public static Object getField(String fieldName, Object classObj) {
		try {

			Field field = null;
			try {
				field = classObj.getClass().getField(fieldName);
			} catch (NoSuchFieldException e) {
				Field[] fields = ClassUtil.getAllFields(classObj.getClass(), Object.class);
				for (Field f : fields) {
					if (f.getName().equalsIgnoreCase(fieldName)) {
						field = f;
						break;
					}
				}
			}

			field.setAccessible(true);
			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
			return field.get(classObj);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	private void setCucumberRunner(boolean cucumberRunner) {
		getBundle().setProperty("cucumber.run.mode", cucumberRunner);
	}

}
