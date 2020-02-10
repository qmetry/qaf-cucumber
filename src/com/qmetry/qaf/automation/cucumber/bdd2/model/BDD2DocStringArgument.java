/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.bdd2.model;

import gherkin.pickles.PickleString;
import io.cucumber.core.gherkin.DocStringArgument;

/**
 * @author chirag.jayswal
 *
 */
public class BDD2DocStringArgument implements DocStringArgument {
	private final PickleString docString;

	BDD2DocStringArgument(PickleString docString) {
		this.docString = docString;
	}

	@Override
	public String getContent() {
		return docString.getContent();
	}

	@Override
	public String getContentType() {
		return docString.getContentType();
	}

	@Override
	public int getLine() {
		return docString.getLocation().getLine();
	}
}
