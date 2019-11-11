package com.qmetry.qaf.automation.cucumber;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import gherkin.ast.TableCell;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;

/**
 * 
 * @author chirag.jayswal
 *
 */
public class Bdd2Pickle extends Pickle {

	private Map<String, Object> metaData;
	private Map<String, Object> testData;

	public Bdd2Pickle(String name, String language, List<PickleStep> steps, List<PickleTag> tags,
			List<PickleLocation> locations) {
		super(name, language, getBdd2Steps(steps), tags, locations);
		initmetaData(tags,locations.get(0));
	}

	public Bdd2Pickle(String name, String language, List<PickleStep> steps, List<PickleTag> tags,
			List<PickleLocation> locations, List<TableCell> headerCells, List<TableCell> valueCells) {
		this(name, language, steps, tags, locations);
		testData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

		Iterator<TableCell> keys = headerCells.iterator();
		Iterator<TableCell> values = valueCells.iterator();
		while (values.hasNext() && keys.hasNext()) {
			testData.put(keys.next().getValue(), values.next().getValue());
		}
	}

	private void initmetaData(List<PickleTag> tags, PickleLocation pickleLocation) {
		metaData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		metaData.put("name", this.getName());
		metaData.put("line", pickleLocation.getLine());

		tags.stream().filter(tag -> tag.getName().contains(":")).forEach(tag -> {
			String[] kv = tag.getName().substring(1).split(":", 2);
			metaData.put(kv[0], kv[1]);
		});
		List<String> groups = tags.stream().filter(tag -> !tag.getName().contains(":")).map(t -> {
			return t.getName().substring(1);
		}).collect(Collectors.toList());
		metaData.put("groups", groups);
	}

	public Map<String, Object> getMetaData() {
		return metaData;
	}

	public Map<String, Object> getTestData() {
		return testData;
	}

	private static List<PickleStep> getBdd2Steps(List<PickleStep> steps) {
		return steps.stream().map(step -> {
			return new Bdd2PickleStep(step.getText(), step.getArgument(), step.getLocations());
		}).collect(Collectors.toList());
	}
}
