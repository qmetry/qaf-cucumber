/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.test;

import com.qmetry.qaf.automation.core.ConfigurationManager;

import io.cucumber.core.cli.Main;

/**
 * @author chirag
 *
 */
public class CucksRunner {

	public static void main(String[] args) {
		// Main m = new Main();
		String[] argv = { "--threads", "3", "-g", "com.qmetry.qaf.automation.cucumber.test", "features", "-p",
				"com.qmetry.qaf.automation.cucumber.QAFCucumberPlugin" };
		Main.main(argv);

		System.out.println(ConfigurationManager.getBundle().getString("cucumber.run.mode", "notfound"));
	}

}
