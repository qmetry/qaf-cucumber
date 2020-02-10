package com.qmetry.qaf.automation.cucumber.bdd2.model;


import gherkin.ast.ScenarioDefinition;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Scenario;
import static com.qmetry.qaf.automation.cucumber.bdd2.model.BDD2Location.from;

public class BDD2Scenario implements Scenario {

	protected final ScenarioDefinition scenarioDefinition;
    protected Location location;

    BDD2Scenario(ScenarioDefinition scenarioDefinition) {
        this.scenarioDefinition = scenarioDefinition;
        location = from(scenarioDefinition.getLocation());
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getKeyWord() {
        return scenarioDefinition.getKeyword();
    }

    @Override
    public String getName() {
        return scenarioDefinition.getName();
    }

}
