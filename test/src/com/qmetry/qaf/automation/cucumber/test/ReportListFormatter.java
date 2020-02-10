package com.qmetry.qaf.automation.cucumber.test;

import java.util.List;
import java.util.Map;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.qmetry.qaf.automation.step.QAFTestStepArgumentFormatter;

import com.qmetry.qaf.automation.cucumber.test.StepDefinitions.Person;

public class ReportListFormatter implements QAFTestStepArgumentFormatter<List<Person>> {

	@Override
	public List<Person> format(Object value, Map<String, Object> context) {
		return new Gson().fromJson(value.toString(), new TypeToken<List<Person>>(){}.getType());
	}

}
