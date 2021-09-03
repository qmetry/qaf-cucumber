/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.bdd2.model;

import static com.qmetry.qaf.automation.cucumber.bdd2.model.BDD2Location.from;
import static java.util.stream.Collectors.toList;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import com.qmetry.qaf.automation.cucumber.bdd2.parser.Bdd2Pickle;

import gherkin.GherkinDialect;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Pickle;
import io.cucumber.core.gherkin.Step;
import io.cucumber.core.gherkin.StepType;

/**
 * @author chirag.jayswal
 *
 */
public class BDD2PickleWrapper implements Pickle {

    private final gherkin.pickles.Pickle pickle;
    private final List<Step> steps;
    private final URI uri;
    private final String keyWord;
    private final String id;

    public BDD2PickleWrapper(gherkin.pickles.Pickle pickle, URI uri, GherkinDocument document, GherkinDialect dialect) {
        this.pickle = pickle;
        this.uri = uri;
        this.steps = createCucumberSteps(pickle, document, dialect, uri.toString());
        this.keyWord = document.getFeature().getChildren().stream()
            .filter(scenarioDefinition -> scenarioDefinition.getLocation().getLine() == getScenarioLocation().getLine())
            .map(ScenarioDefinition::getKeyword)
            .findFirst()
            .orElse("Scenario");
        this.id = pickle.getName() + ":" + pickle.getLocations()
            .stream()
            .map(l -> String.valueOf(l.getLine()))
            .collect(Collectors.joining(":"));

    }

    private static List<Step> createCucumberSteps(gherkin.pickles.Pickle pickle, GherkinDocument document, GherkinDialect dialect, String uri) {
        List<Step> list = new ArrayList<>();
        String previousGivenWhenThen = dialect.getGivenKeywords()
            .stream()
            .filter(s -> !StepType.isAstrix(s))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No Given keyword for dialect: " + dialect.getName()));

        for (PickleStep step : pickle.getSteps()) {
            Step cucumberStep = new Bdd2Step(step, document, dialect, previousGivenWhenThen, uri);
            if (cucumberStep.getType().isGivenWhenThen()) {
                previousGivenWhenThen = cucumberStep.getKeyWord();
            }
            list.add(cucumberStep);
        }
        return list;
    }

    @Override
    public String getKeyword() {
        return keyWord;
    }

    @Override
    public String getLanguage() {
        return pickle.getLanguage();
    }

    @Override
    public String getName() {
        return pickle.getName();
    }


    @Override
    public Location getLocation() {
        return from(pickle.getLocations().get(0));
    }

    @Override
    public Location getScenarioLocation() {
        int last = pickle.getLocations().size() - 1;
        return from(pickle.getLocations().get(last));
    }

    @Override
    public List<Step> getSteps() {
        return steps;
    }

    @Override
    public List<String> getTags() {
        return pickle.getTags().stream()
            .map(PickleTag::getName)
            .collect(toList());
    }

    @Override
    public URI getUri() {
        return uri;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BDD2PickleWrapper other = (BDD2PickleWrapper) o;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

	public Map<String, Object> getTestData() {
		return  ((Bdd2Pickle)pickle).getTestData();
	}

	public Map<String, Object> getMetaData() {
		return  ((Bdd2Pickle)pickle).getMetaData();
	}
	
	public void setMetaData(Map<String, Object> metadata) {
		 ((Bdd2Pickle)pickle).setMetaData(metadata);
	}

}
