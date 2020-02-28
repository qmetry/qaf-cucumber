/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.test;

import io.cucumber.java.en.Given;

/**
 * @author chirag
 *
 */
public class CucumberStepDefs {

	@Given("I have {int} \\{what} cucumber(s) in my belly/stomach \\(amazing!)")
	public void stpeWithEsc(int cnt) {
		System.out.println("stpeWithEsc" +cnt);
	}
}
