/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.hamcrest.Matchers;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.qmetry.qaf.automation.cucumber.CucumberStepMatcher;
import com.qmetry.qaf.automation.util.JSONUtil;
import com.qmetry.qaf.automation.util.Validator;

/**
 * @author chirag
 *
 */
public class CucumberStepMatcherTest {
	
	//@MetaData("{'indices':[10]}")
	@Test(dataProvider="stepMatchDataProvider")
	public void testMatcher(String def, String call, boolean match, String expectedArgs) {
		CucumberStepMatcher stepMatcher = new CucumberStepMatcher();
		boolean res = stepMatcher.matches(def, call, Collections.emptyMap());
		Validator.assertThat(res, Matchers.equalTo(match));
		if(match) {
			List<String[]> argsFromCall = stepMatcher.getArgsFromCall(def, call, Collections.emptyMap());
			Validator.assertThat(JSONUtil.toString(argsFromCall), Matchers.equalToIgnoringCase(expectedArgs));
		}
	}

	@DataProvider(name="stepMatchDataProvider")
	public Iterator<Object[]> stepMatchDataProvider() {
		List<Object[]> testData = new ArrayList<Object[]>();
		
		testData.add(new Object[] {"i have {string} cuckes in my bucket", "i have '2' cuckes in my bucket",true,"[[\"2\",\"STRING\"]]"});
		testData.add(new Object[] {"i have {int} cuckes in my bucket", "i have 2 cuckes in my bucket",true,"[[\"2\",\"LONG\"]]"});
		testData.add(new Object[] {"i have {cnt} cuckes in my bucket", "i have 2 cuckes in my bucket",true,"[[\"2\",\"LONG\"]]"});

		testData.add(new Object[] {"i have {int} cuckes in my {string}", "i have 2 cuckes in my 'bucket'",true,"[[\"2\",\"LONG\"],[\"bucket\",\"STRING\"]]"});
		testData.add(new Object[] {"i have {int} cuckes in my {string}", "i have 2 cuckes in my \"bucket\"",true,"[[\"2\",\"LONG\"],[\"bucket\",\"STRING\"]]"});
		testData.add(new Object[] {"i have {float} cuckes in my {string}", "i have 42.5 cuckes in my \"bucket\"",true,"[[\"42.5\",\"DOUBLE\"],[\"bucket\",\"STRING\"]]"});
		testData.add(new Object[] {"I have {int} cucumber(s) in my belly/stomach", "I have 42 cucumbers in my belly",true,"[[\"42\",\"LONG\"]]"});
		testData.add(new Object[] {"I have {int} cucumber(s) in my belly/stomach", "I have 42 cucumbers in my stomach",true,"[[\"42\",\"LONG\"]]"});
		testData.add(new Object[] {"I have {int} cucumber(s) in my belly/stomach", "I have 1 cucumber in my stomach",true,"[[\"1\",\"LONG\"]]"});
		testData.add(new Object[] {"I have {int} \\{what} cucumber(s) in my belly/stomach", "I have 1 {what} cucumber in my stomach",true,"[[\"1\",\"LONG\"]]"});

		testData.add(new Object[] {"I have {int} cucumbers in my stomach \\(amazing!)", "I have 1 cucumbers in my stomach (amazing!)",true,"[[\"1\",\"LONG\"]]"});
		testData.add(new Object[] {"I have {int} \\{what} cucumber(s) in my belly/stomach \\(amazing!)", "I have 1 {what} cucumber in my stomach (amazing!)",true,"[[\"1\",\"LONG\"]]"});

		return testData.iterator();
	}
	
	public static void main(String[] args) {
		CucumberStepMatcher stepMatcher = new CucumberStepMatcher();
		boolean res = stepMatcher.matches("i have {String} cuckes in my bucket", "i have '2' cuckes in my bucket", Collections.emptyMap());
		System.out.println(res);
		List<String[]> argsFromCall = stepMatcher.getArgsFromCall("i have {String} cuckes in my bucket", "i have '2' cuckes in my bucket", Collections.emptyMap());
		System.out.println(JSONUtil.toString(argsFromCall));
		
	}
}
