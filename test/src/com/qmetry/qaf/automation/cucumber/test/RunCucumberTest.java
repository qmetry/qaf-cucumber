package com.qmetry.qaf.automation.cucumber.test;

import io.cucumber.junit.CucumberOptions;
import io.cucumber.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(dryRun=false,features="features", plugin = {"com.qmetry.qaf.automation.cucumber.QAFCucumberPlugin","pretty","html:report.html"})
public class RunCucumberTest {
}
