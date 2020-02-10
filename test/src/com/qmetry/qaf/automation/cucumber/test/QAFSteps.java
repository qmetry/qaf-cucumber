package com.qmetry.qaf.automation.cucumber.test;

import java.util.List;
import java.util.Map;

import com.qmetry.qaf.automation.step.QAFTestStep;
import com.qmetry.qaf.automation.testng.report.ReportEntry;

import io.cucumber.java.Status;
public class QAFSteps {
	@QAFTestStep(description = "user request {call} with data:{data}")
	public void step1(String s, Map<String, Object> data) {
		System.out.println(s + ":" + data);
	}
	
	@QAFTestStep(description = "user request {call} with list:{list}")
	public void step3(String s, List<Object> data) {
		System.out.println(s + ":" + data);
		System.out.println(s + ":" + data.size());

	}
	@QAFTestStep(description = "user request {call} with map {data}")
	public void step2(String s, Map<String, Object> data) {
		System.out.println(s + ":" + data);
	}
	
	@QAFTestStep(description = "test {status} during execution")
	public void stepWithOBJ(Status s ) {
		System.out.println("Status " + s);
	}
	
	@QAFTestStep(description = "create new report entry:{reportEntry}")
	public void newReport(ReportEntry[] reportEntry) {
		System.out.println("ReportEntry " + reportEntry);
		System.out.println("ReportEntry " + reportEntry[0].getClass());

	}
	
	@QAFTestStep(description = "create new {reportEntry} report entry")
	public void newReportEntry(ReportEntry reportEntry) {
		System.out.println("ReportEntry " + reportEntry);
	}
}
