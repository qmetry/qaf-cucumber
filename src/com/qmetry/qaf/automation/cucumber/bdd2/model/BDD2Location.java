package com.qmetry.qaf.automation.cucumber.bdd2.model;

import java.util.Objects;

import gherkin.pickles.PickleLocation;
import io.cucumber.core.gherkin.Location;

public class BDD2Location implements Location {

    private final int line;
    private final int column;

    private BDD2Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    static Location from(PickleLocation location) {
    	if(null==location) {
            return new BDD2Location(0, 0);
    	}
        return new BDD2Location(location.getLine(), location.getColumn());
    }

    public static Location from(gherkin.ast.Location location) {
    	if(null==location) {
            return new BDD2Location(0, 0);
    	}
        return new BDD2Location(location.getLine(), location.getColumn());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BDD2Location other = (BDD2Location) o;
        return line == other.line &&
            column == other.column;
    }

    @Override
    public int getLine() {
        return line;
    }

    @Override
    public int getColumn() {
        return column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column);
    }
}
