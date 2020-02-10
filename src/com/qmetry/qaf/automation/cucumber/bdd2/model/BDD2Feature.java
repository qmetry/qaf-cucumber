/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.bdd2.model;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioOutline;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.Located;
import io.cucumber.core.gherkin.Location;
import io.cucumber.core.gherkin.Node;
import io.cucumber.core.gherkin.Pickle;

/**
 * @author chirag.jayswal
 *
 */
public class BDD2Feature implements Feature {

	private final URI uri;
	private final List<Pickle> pickles;
	private final GherkinDocument gherkinDocument;
	private final List<Node> children;
	private final String bdd2Source;
	private Location location;

	public BDD2Feature(GherkinDocument gherkinDocument, URI uri, String bdd2Source, List<Pickle> pickles) {
		this.uri = uri;
		this.gherkinDocument = gherkinDocument;
		this.bdd2Source = bdd2Source;
		this.pickles = pickles;
		location = BDD2Location.from(gherkinDocument.getFeature().getLocation());

		this.children = gherkinDocument.getFeature().getChildren().stream().map(scenarioDefinition -> {
			if (scenarioDefinition instanceof ScenarioOutline) {
				return new BDD2ScenarioOutline(scenarioDefinition);
			}
			return new BDD2Scenario(scenarioDefinition);
		}).map(Node.class::cast).collect(Collectors.toList());
	}

	@Override
	public Collection<Node> children() {
		return children;
	}

	@Override
	public String getKeyword() {
		return gherkinDocument.getFeature().getKeyword();
	}

	@Override
	public Optional<Pickle> getPickleAt(Located located) {
		Location location = located.getLocation();
		return pickles.stream().filter(cucumberPickle -> cucumberPickle.getLocation().equals(location)).findFirst();
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public List<Pickle> getPickles() {
		return pickles;
	}

	@Override
	public String getKeyWord() {
		return null;
	}

	@Override
	public String getName() {
		return gherkinDocument.getFeature().getName();
	}

	@Override
	public URI getUri() {
		return uri;
	}

	@Override
	public String getSource() {
		return bdd2Source;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BDD2Feature other = (BDD2Feature) o;
		return uri.equals(other.uri);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uri);
	}

}
