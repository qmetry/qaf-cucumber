package com.qmetry.qaf.automation.cucumber.bdd2.model;


import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import gherkin.ast.ScenarioDefinition;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.ScenarioOutline;

public class BDD2ScenarioOutline extends BDD2Scenario implements ScenarioOutline {
	private final List<Examples> children;

    BDD2ScenarioOutline(ScenarioDefinition scenarioDefinition) {
        super(scenarioDefinition);
        this.children = ((gherkin.ast.ScenarioOutline) scenarioDefinition).getExamples().stream()
                .map(BDD2Examples::new)
                .collect(Collectors.toList());
    }

	@Override
	public Collection<Examples> children() {
        return children;
	}

}
