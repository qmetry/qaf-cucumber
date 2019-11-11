package com.qmetry.qaf.automation.cucumber;

import java.util.List;

import com.qmetry.qaf.automation.core.ConfigurationManager;

import gherkin.pickles.Argument;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;

/**
 * 
 * @author chirag.jayswal
 *
 */
public class Bdd2PickleStep extends PickleStep {

	public Bdd2PickleStep(String text, List<Argument> arguments, List<PickleLocation> locations) {
		super(text, arguments, locations);
	}
	
	@Override
	public String getText() {
		return ConfigurationManager.getBundle().getSubstitutor().replace(super.getText());
	}

}
