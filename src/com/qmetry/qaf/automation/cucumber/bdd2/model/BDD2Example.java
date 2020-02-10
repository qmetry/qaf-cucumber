/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.bdd2.model;

import gherkin.ast.TableRow;
import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Location;

/**
 * @author chirag
 *
 */
public class BDD2Example implements Example {

	private final int rowIndex;
	private Location location;

	BDD2Example(TableRow tableRow, int rowIndex) {
		this.rowIndex = rowIndex;
		this.location = BDD2Location.from(tableRow.getLocation());
	}

	@Override
	public String getKeyWord() {
		return null;
	}

	public String getName() {
		return "Example #" + rowIndex;
	}

	@Override
	public Location getLocation() {
		return location;
	}
}
