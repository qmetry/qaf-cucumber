package com.qmetry.qaf.automation.cucumber;

import java.util.List;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

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
		return getBundle().getSubstitutor().replace(super.getText());
	}

}
