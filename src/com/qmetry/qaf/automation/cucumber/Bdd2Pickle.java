package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.data.MetaDataScanner.formatMetaData;

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
			List<PickleLocation> locations, Map<String, Object> metaData) {
		super(name, language, getBdd2Steps(steps), tags, locations);
		initMetaData(metaData);
	}

	public Bdd2Pickle(String name, String language, List<PickleStep> steps, List<PickleTag> tags,
			List<PickleLocation> locations, List<TableCell> headerCells, List<TableCell> valueCells, Map<String, Object> metaData) {
		this(name, language, steps, tags, locations, metaData);
		testData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

		Iterator<TableCell> keys = headerCells.iterator();
		Iterator<TableCell> values = valueCells.iterator();
		while (values.hasNext() && keys.hasNext()) {
			testData.put(keys.next().getValue(), values.next().getValue());
		}
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
	
	private void initMetaData(Map<String, Object> inMetaData) {
		metaData=new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		metaData.putAll(inMetaData);
		metaData.put("name", getName());
		formatMetaData(metaData);
	}
}
