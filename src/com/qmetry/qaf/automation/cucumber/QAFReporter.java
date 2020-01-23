/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static com.qmetry.qaf.automation.util.JSONUtil.getJsonObjectFromFile;
import static com.qmetry.qaf.automation.util.JSONUtil.writeJsonObjectToFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.impl.LogFactoryImpl;

import com.qmetry.qaf.automation.core.CheckpointResultBean;
import com.qmetry.qaf.automation.core.LoggingBean;
import com.qmetry.qaf.automation.keys.ApplicationProperties;
import com.qmetry.qaf.automation.testng.report.ClassInfo;
import com.qmetry.qaf.automation.testng.report.MetaInfo;
import com.qmetry.qaf.automation.testng.report.MethodInfo;
import com.qmetry.qaf.automation.testng.report.MethodResult;
import com.qmetry.qaf.automation.testng.report.Report;
import com.qmetry.qaf.automation.testng.report.ReportEntry;
import com.qmetry.qaf.automation.testng.report.ReporterUtil;
import com.qmetry.qaf.automation.testng.report.TestOverview;
import com.qmetry.qaf.automation.util.DateUtil;
import com.qmetry.qaf.automation.util.FileUtil;
import com.qmetry.qaf.automation.util.StringUtil;

/**
 * Utility class for QAF reporting used by cucumber plugin.
 * @author chirag.jayswal
 *
 */
public class QAFReporter {
	private static final Log logger = LogFactoryImpl.getLog(ReporterUtil.class);
	private static final String QAF_TEST_IDENTIFIER = "qaf_test_identifier";
	private static AtomicInteger passCnt = new AtomicInteger(0);
	private static AtomicInteger failCnt = new AtomicInteger(0);
	private static AtomicInteger skipCnt = new AtomicInteger(0);
	private static AtomicInteger indexer = new AtomicInteger(0);

	public static void updateMetaInfo() {
		createMetaInfo(false);
	}

	public static void createMetaInfo() {
		createMetaInfo(true);
		updateOverview(null, true);
	}

	private static void createMetaInfo(boolean listEntry) {

		String suiteName = getBundle().getString("suite.name",
				new File(".").getAbsoluteFile().getParentFile().getName());
		List<String> testNames = new ArrayList<String>();
		testNames.add(getTestName());

		String dir = ApplicationProperties.JSON_REPORT_DIR.getStringVal();
		Report report = new Report();

		if (!getBundle().containsKey("suit.start.ts")) {
			dir = ApplicationProperties.JSON_REPORT_DIR
					.getStringVal(ApplicationProperties.JSON_REPORT_ROOT_DIR.getStringVal("test-results") + "/"
							+ DateUtil.getDate(0, "EdMMMyy_hhmmssa"));
			getBundle().setProperty(ApplicationProperties.JSON_REPORT_DIR.key, dir);
			FileUtil.checkCreateDir(ApplicationProperties.JSON_REPORT_ROOT_DIR.getStringVal("test-results"));
			FileUtil.checkCreateDir(dir);
			getBundle().setProperty("suit.start.ts", System.currentTimeMillis());
		} else {
			report.setEndTime(System.currentTimeMillis());
		}
		report.setName(suiteName);
		report.setTests(testNames);
		report.setDir(dir);

		int pass = passCnt.get(), fail = failCnt.get(), skip = skipCnt.get(), total = 0;
		report.setPass(pass);
		report.setFail(fail);
		report.setSkip(skip);
		report.setTotal((pass + fail + skip) > total ? pass + fail + skip : total);
		report.setStatus(fail > 0 ? "fail" : pass > 0 ? "pass" : "unstable");
		report.setStartTime(getBundle().getLong("suit.start.ts", 0));

		appendReportInfo(report);
		if (listEntry) {
			ReportEntry reportEntry = new ReportEntry();
			reportEntry.setName(suiteName);
			reportEntry.setStartTime(getBundle().getLong("suit.start.ts", 0));
			reportEntry.setDir(dir);
			appendMetaInfo(reportEntry);
		}
	}

	public static synchronized void updateOverview(String classname, boolean evnEntry) {
		try {
			String file = ApplicationProperties.JSON_REPORT_DIR.getStringVal() + "/" + getTestName() + "/overview.json";
			TestOverview overview = getJsonObjectFromFile(file, TestOverview.class);
			if (evnEntry) {
				Map<String, Object> runPrams = new HashMap<String, Object>();
				Configuration env = getBundle().subset("env");
				Iterator<?> iter = env.getKeys();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					runPrams.put(key, env.getString(key));
				}
				Map<String, Object> envInfo = new HashMap<String, Object>();
				envInfo.put("isfw-build-info", getBundle().getObject("isfw.build.info"));
				envInfo.put("run-parameters", runPrams);
				envInfo.put("browser-desired-capabilities", getBundle().getObject("driver.desiredCapabilities"));
				envInfo.put("browser-actual-capabilities", getActualCapabilities());

				overview.setEnvInfo(envInfo);
				Map<String, Object> executionEnvInfo = new HashMap<String, Object>();
				executionEnvInfo.put("os.name", System.getProperty("os.name"));
				executionEnvInfo.put("os.version", System.getProperty("os.version"));

				executionEnvInfo.put("os.arch", System.getProperty("os.arch"));
				executionEnvInfo.put("java.version", System.getProperty("java.version"));
				executionEnvInfo.put("java.vendor", System.getProperty("java.vendor"));
				executionEnvInfo.put("java.arch", System.getProperty("sun.arch.data.model"));

				executionEnvInfo.put("user.name", System.getProperty("user.name"));
				try {
					executionEnvInfo.put("host", InetAddress.getLocalHost().getHostName());
				} catch (Exception e) {
					// This code added for MAC to fetch hostname
					String hostname = execHostName("hostname");
					executionEnvInfo.put("host", hostname);
				}
				envInfo.put("execution-env-info", executionEnvInfo);
			}

			int pass = passCnt.get();
			int fail = failCnt.get();
			int skip = skipCnt.get();
			int total = pass + fail + skip;

			overview.setTotal(total > (pass + fail + skip) ? total : pass + fail + skip);
			overview.setPass(pass);
			overview.setSkip(skip);
			overview.setFail(fail);
			if (null != classname) {
				overview.getClasses().add(classname);
			}
			if ((overview.getStartTime() > 0)) {
				overview.setEndTime(System.currentTimeMillis());
			} else {
				overview.setStartTime(System.currentTimeMillis());
			}
			writeJsonObjectToFile(file, overview);
			updateMetaInfo();
		} catch (Exception e) {
			logger.debug(e);
		}
	}

	private static Map<String, String> getActualCapabilities() {
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) getBundle().getObject("driver.actualCapabilities");
		Map<String, String> newMap = new HashMap<String, String>();
		if (null != map) {
			for (String key : map.keySet()) {
				try {
					newMap.put(key, String.valueOf(map.get(key)));
				} catch (Exception e) {

				}
			}
		}
		return newMap;
	}


	/**
	 * should be called on test method completion

	 * @param className
	 * @param bdd2Pickle
	 * @param durationMs
	 * @param result
	 * @param error
	 * @param logs
	 * @param checkpoints
	 */
	public static void createMethodResult(String className, Bdd2Pickle bdd2Pickle, long durationMs, String result, Throwable error, List<LoggingBean> logs,
			List<CheckpointResultBean> checkpoints) {

		try {
			//String className = tc.getScenarioDesignation().substring(0, tc.getScenarioDesignation().indexOf(".feature"))
			//		.replaceAll("/", ".");
			String classdir = getClassDir(getTestName() + "/" + className);

			MethodResult methodResult = new MethodResult();

			methodResult.setSeleniumLog(logs);
			methodResult.setCheckPoints(checkpoints);
			methodResult.setThrowable(error);
			// TODO::
			// updateOverview(tc, bdd2Pickle);
			String fileName = getMethodIdentifier(bdd2Pickle);// StringUtil.toTitleCaseIdentifier(getMethodName(result));
			String methodResultFile = classdir + "/" + fileName;

			File f = new File(methodResultFile + ".json");
			bdd2Pickle.getMetaData().remove(QAF_TEST_IDENTIFIER);

			if (f.exists()) {
				// if file already exists then it will append some unique
				// character as suffix
				String suffix = "_"+indexer.incrementAndGet();
				fileName += suffix;
				// add updated file name as 'resultFileName' key in metaData
				methodResultFile = classdir + "/" + fileName;

				updateClassMetaInfo(bdd2Pickle, durationMs, result, fileName, className);
			} else {
				updateClassMetaInfo( bdd2Pickle, durationMs, result, fileName, className);
			}

			writeJsonObjectToFile(methodResultFile + ".json", methodResult);
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

	}

	/**
	 * should be called on test method completion
	 * 
	 * @param context
	 * @param result
	 * @param dir2
	 */
	private static synchronized void updateClassMetaInfo(Bdd2Pickle bdd2Pickle, long durationMs, String result,
			String methodfname, String classname) {
		String dir = getClassDir(getTestName() + "/" + classname);
		String file = dir + "/meta-info.json";
		FileUtil.checkCreateDir(dir);

		ClassInfo classInfo = getJsonObjectFromFile(file, ClassInfo.class);

		MethodInfo methodInfo = new MethodInfo();

		methodInfo.setStartTime(System.currentTimeMillis() - durationMs);
		methodInfo.setDuration(durationMs);

		Map<String, Object> metadata = bdd2Pickle.getMetaData();
		if (null != bdd2Pickle.getTestData()) {
			methodInfo.setArgs(new Object[] { bdd2Pickle.getTestData() });
		}
		
		methodInfo.setMetaData(metadata);
		
		methodInfo.setType("test");
		
		methodInfo.setResult(getResult(result));

		if (StringUtil.isNotBlank(methodfname)) {
			metadata.put("resultFileName", methodfname);
		}

		updateOverview(classname, false);
		if (!classInfo.getMethods().contains(methodInfo)) {
			logger.debug("method:  result: " + methodInfo.getResult() + " groups: " + methodInfo.getMetaData());
			classInfo.getMethods().add(methodInfo);
			writeJsonObjectToFile(file, classInfo);
		} else {
			logger.warn("methodInfo already wrritten for " + methodInfo.getName());
		}
	}

	private static String getMethodName(Bdd2Pickle bdd2Pickle) {
		return bdd2Pickle.getName();
	}

	private static String getMethodIdentifier(Bdd2Pickle bdd2Pickle) {

		String id = getMethodName(bdd2Pickle);
		String identifierKey = ApplicationProperties.TESTCASE_IDENTIFIER_KEY.getStringVal("testCaseId");

		Map<String, Object> metadata = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

		metadata.putAll(bdd2Pickle.getMetaData());
		if (bdd2Pickle.getTestData() != null) {
			metadata.putAll(bdd2Pickle.getTestData());
		}
		String idFromMetaData = metadata.getOrDefault(identifierKey,"").toString();
		if (StringUtil.isNotBlank(idFromMetaData) ) {
			id = idFromMetaData;
		}
		id = StringUtil.toTitleCaseIdentifier(id);

		if (id.length() > 45) {
			id = id.substring(0, 45);
		}
		bdd2Pickle.getMetaData().put(QAF_TEST_IDENTIFIER, id);
		return (String) bdd2Pickle.getMetaData().get(QAF_TEST_IDENTIFIER);
	}

	private static String getClassDir(String dir) {
		return ApplicationProperties.JSON_REPORT_DIR.getStringVal("test-results") + "/" + dir;
	}

	private static void appendReportInfo(Report report) {

		String file = report.getDir() + "/meta-info.json";
		writeJsonObjectToFile(file, report);
	}

	private static void appendMetaInfo(ReportEntry report) {

		String file = ApplicationProperties.JSON_REPORT_ROOT_DIR.getStringVal("test-results") + "/meta-info.json";
		MetaInfo metaInfo = getJsonObjectFromFile(file, MetaInfo.class);
		metaInfo.getReports().remove(report);
		metaInfo.getReports().add(report);
		writeJsonObjectToFile(file, metaInfo);
	}

	private static String getResult(String result) {
		switch (result.toUpperCase().charAt(0)) {
		case 'P':
			passCnt.incrementAndGet();
			return "pass";
		case 'F':
			failCnt.incrementAndGet();
			return "fail";
		default:
			skipCnt.incrementAndGet();
			return "skip";
		}
	}

	private static String getTestName() {
		return getBundle().getString("testname", "BDD2");
	}

	public static String execHostName(String execCommand) {
		InputStream stream;
		Scanner s;
		try {
			Process proc = Runtime.getRuntime().exec(execCommand);
			stream = proc.getInputStream();
			if (stream != null) {
				s = new Scanner(stream);
				s.useDelimiter("\\A");
				String val = s.hasNext() ? s.next() : "";
				stream.close();
				s.close();
				return val;
			}
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
		return "";
	}
}
