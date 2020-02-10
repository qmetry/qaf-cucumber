/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.bdd2.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import io.cucumber.core.gherkin.Example;
import io.cucumber.core.gherkin.Examples;
import io.cucumber.core.gherkin.Location;

/**
 * @author chirag
 *
 */
public class BDD2Examples implements Examples {

	private final List<Example> children;
    private final gherkin.ast.Examples examples;
    private final Location location;

    BDD2Examples(gherkin.ast.Examples examples) {
        this.examples = examples;
        this.location=BDD2Location.from(examples.getLocation());
        if (examples.getTableBody() == null) {
            this.children = Collections.emptyList();
        } else {
            AtomicInteger rowCounter = new AtomicInteger(1);
            this.children = examples.getTableBody().stream()
                .map(tableRow -> new BDD2Example(tableRow, rowCounter.getAndIncrement()))
                .collect(Collectors.toList());
        }
    }
    
    /*BDD2Examples(List<Map<String, Object>> examples) {
        this.examples = examples;
        this.location=BDD2Location.from(examples.getLocation());
        if (examples.getTableBody() == null) {
            this.children = Collections.emptyList();
        } else {
            AtomicInteger rowCounter = new AtomicInteger(1);
            this.children = examples.getTableBody().stream()
                .map(tableRow -> new BDD2Example(tableRow, rowCounter.getAndIncrement()))
                .collect(Collectors.toList());
        }
    }*/

    @Override
    public Collection<Example> children() {
        return children;
    }

    @Override
    public Location getLocation() {
        return location;
    }

    @Override
    public String getKeyWord() {
        return examples.getKeyword();
    }

    @Override
    public String getName() {
        return examples.getName();
    }
}
