package com.qmetry.qaf.automation.cucumber;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.qmetry.qaf.automation.testng.dataprovider.DataProviderUtil;
import com.qmetry.qaf.automation.testng.dataprovider.QAFDataProvider.params;
import com.qmetry.qaf.automation.util.CSVUtil;
import com.qmetry.qaf.automation.util.ClassUtil;
import com.qmetry.qaf.automation.util.DatabaseUtil;
import com.qmetry.qaf.automation.util.ExcelUtil;
import com.qmetry.qaf.automation.util.JSONUtil;

import gherkin.SymbolCounter;
import gherkin.ast.Background;
import gherkin.ast.DataTable;
import gherkin.ast.DocString;
import gherkin.ast.Examples;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Location;
import gherkin.ast.Node;
import gherkin.ast.Scenario;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.ScenarioOutline;
import gherkin.ast.Step;
import gherkin.ast.TableCell;
import gherkin.ast.TableRow;
import gherkin.ast.Tag;
import gherkin.pickles.Argument;
import gherkin.pickles.Pickle;
import gherkin.pickles.PickleCell;
import gherkin.pickles.PickleLocation;
import gherkin.pickles.PickleRow;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleString;
import gherkin.pickles.PickleTable;
import gherkin.pickles.PickleTag;

public class Bdd2Compiler {

	public List<Pickle> compile(GherkinDocument gherkinDocument) {
		List<Pickle> pickles = new ArrayList<>();
		Feature feature = gherkinDocument.getFeature();
		if (feature == null) {
			return pickles;
		}

		String language = feature.getLanguage();
		List<Tag> featureTags = feature.getTags();
		List<PickleStep> backgroundSteps = new ArrayList<>();

		for (ScenarioDefinition scenarioDefinition : feature.getChildren()) {
			if (scenarioDefinition instanceof Background) {
				backgroundSteps = pickleSteps(scenarioDefinition);
			} else if (scenarioDefinition instanceof Scenario) {
				compileScenario(pickles, backgroundSteps, (Scenario) scenarioDefinition, featureTags, language);
			} else {
				compileScenarioOutline(pickles, backgroundSteps, (ScenarioOutline) scenarioDefinition, featureTags,
						language);
			}
		}
		return pickles;
	}

	private void compileScenario(List<Pickle> pickles, List<PickleStep> backgroundSteps, Scenario scenario,
			List<Tag> featureTags, String language) {
		List<PickleStep> steps = new ArrayList<>();
		if (!scenario.getSteps().isEmpty())
			steps.addAll(backgroundSteps);

		List<Tag> scenarioTags = new ArrayList<>();
		scenarioTags.addAll(featureTags);
		scenarioTags.addAll(scenario.getTags());

		steps.addAll(pickleSteps(scenario));

		Pickle pickle = new Bdd2Pickle(scenario.getName(), language, steps, pickleTags(scenarioTags),
				singletonList(pickleLocation(scenario.getLocation())));
		pickles.add(pickle);
	}

	private void compileScenarioOutline(List<Pickle> pickles, List<PickleStep> backgroundSteps,
			ScenarioOutline scenarioOutline, List<Tag> featureTags, String language) {
		List<Tag> scenariotags = new ArrayList<>();
		scenariotags.addAll(featureTags);
		scenariotags.addAll(scenarioOutline.getTags());
		List<Examples> exteranalExamples = getExamples(scenariotags,scenarioOutline.getLocation());
		List<Examples> examplesToUse = exteranalExamples != null ? exteranalExamples : scenarioOutline.getExamples();
		if (exteranalExamples != null) {
			ClassUtil.setField("examples", scenarioOutline, exteranalExamples);
		}

		for (final Examples examples : examplesToUse) {
			if (examples.getTableHeader() == null)
				continue;
			List<TableCell> variableCells = examples.getTableHeader().getCells();
			for (final TableRow values : examples.getTableBody()) {
				List<TableCell> valueCells = values.getCells();

				List<PickleStep> steps = new ArrayList<>();
				if (!scenarioOutline.getSteps().isEmpty())
					steps.addAll(backgroundSteps);

				List<Tag> tags = new ArrayList<>(scenariotags);
				tags.addAll(examples.getTags());

				for (Step scenarioOutlineStep : scenarioOutline.getSteps()) {
					String stepText = interpolate(scenarioOutlineStep.getText(), variableCells, valueCells);

					PickleStep pickleStep = new PickleStep(stepText,
							createPickleArguments(scenarioOutlineStep.getArgument(), variableCells, valueCells),
							asList(pickleLocation(values.getLocation()), pickleStepLocation(scenarioOutlineStep)));
					steps.add(pickleStep);
				}

				Pickle pickle = new Bdd2Pickle(interpolate(scenarioOutline.getName(), variableCells, valueCells), language,
						steps, pickleTags(tags),
						asList(pickleLocation(values.getLocation()), pickleLocation(scenarioOutline.getLocation())),examples.getTableHeader().getCells(),valueCells);

				pickles.add(pickle);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<Examples> getExamples(List<Tag> scenariotags, Location location) {
		Map<String, Object> metadata = new HashMap<String, Object>();
		List<Examples> listToReturn = new ArrayList<Examples>();

		scenariotags.stream().filter(tag -> tag.getName().contains(":")).forEach(tag -> {
			String[] kv = tag.getName().substring(1).split(":", 2);
			metadata.put(kv[0].toUpperCase(), kv[1]);
		});

		List<Object[]> externalData = getData(metadata);

		if (null == externalData) {
			return null;
		}
		TableRow tableHeader = new TableRow(location,
				((Map<String, Object>) externalData.get(0)[0]).keySet().stream().map(key -> {
					return new TableCell(location, key);
				}).collect(Collectors.toList()));

		List<TableRow> tableBody = externalData.stream().map(o -> {
			return new TableRow(location, ((Map<String, Object>) o[0]).values().stream().map(val -> {
				return new TableCell(location, val.toString());
			}).collect(Collectors.toList()));
		}).collect(Collectors.toList());

		listToReturn.add(new Examples(location, new ArrayList<>(), null, null, null, tableHeader, tableBody));

		return listToReturn;
	}

	private static List<Object[]> getData(Map<String, Object> metadata) {

		String query = (String) metadata.get(params.SQLQUERY.name());
		if (isNotBlank(query)) {
			return Arrays.asList(DatabaseUtil.getRecordDataAsMap(query));
		}

		String jsonTable = (String) metadata.get(params.JSON_DATA_TABLE.name());
		if (isNotBlank(jsonTable)) {
			return Arrays.asList(JSONUtil.getJsonArrayOfMaps(jsonTable));
		}

		String file = (String) metadata.get(params.DATAFILE.name());
		String key = (String) metadata.get(params.KEY.name());

		if (isNotBlank(file)) {
			if (file.endsWith("json")) {
				return Arrays.asList(JSONUtil.getJsonArrayOfMaps(file));
			}
			if (file.endsWith("xml")) {
				List<Object[]> mapData = DataProviderUtil.getDataSetAsMap(key, file);
				return mapData;
			}
			if (file.endsWith("xls")) {
				if (isNotBlank(key)) {
					return Arrays.asList(ExcelUtil.getTableDataAsMap(file, ((String) metadata.get(params.KEY.name())),
							(String) metadata.get(params.SHEETNAME.name())));
				}
				return Arrays.asList(ExcelUtil.getExcelDataAsMap(file, (String) metadata.get(params.SHEETNAME.name())));
			}
			// csv, text
			List<Object[]> csvData = CSVUtil.getCSVDataAsMap(file);
			return csvData;
		}
		if (isNotBlank(key)) {
			List<Object[]> mapData = DataProviderUtil.getDataSetAsMap(key, "");
			return mapData;
		}
		return null;
		// throw new RuntimeException("No data provider found");
	}

	private List<Argument> createPickleArguments(Node argument) {
		List<TableCell> noCells = emptyList();
		return createPickleArguments(argument, noCells, noCells);
	}

	private List<Argument> createPickleArguments(Node argument, List<TableCell> variableCells,
			List<TableCell> valueCells) {
		List<Argument> result = new ArrayList<>();
		if (argument == null)
			return result;
		if (argument instanceof DataTable) {
			DataTable t = (DataTable) argument;
			List<TableRow> rows = t.getRows();
			List<PickleRow> newRows = new ArrayList<>(rows.size());
			for (TableRow row : rows) {
				List<TableCell> cells = row.getCells();
				List<PickleCell> newCells = new ArrayList<>();
				for (TableCell cell : cells) {
					newCells.add(new PickleCell(pickleLocation(cell.getLocation()),
							interpolate(cell.getValue(), variableCells, valueCells)));
				}
				newRows.add(new PickleRow(newCells));
			}
			result.add(new PickleTable(newRows));
		} else if (argument instanceof DocString) {
			DocString ds = (DocString) argument;
			result.add(new PickleString(pickleLocation(ds.getLocation()),
					interpolate(ds.getContent(), variableCells, valueCells),
					ds.getContentType() == null ? null : interpolate(ds.getContentType(), variableCells, valueCells)));
		} else {
			throw new RuntimeException("Unexpected argument type: " + argument);
		}
		return result;
	}

	private List<PickleStep> pickleSteps(ScenarioDefinition scenarioDefinition) {
		List<PickleStep> result = new ArrayList<>();
		for (Step step : scenarioDefinition.getSteps()) {
			result.add(pickleStep(step));
		}
		return unmodifiableList(result);
	}

	private PickleStep pickleStep(Step step) {
		return new PickleStep(step.getText(), createPickleArguments(step.getArgument()),
				singletonList(pickleStepLocation(step)));
	}

	private String interpolate(String name, List<TableCell> variableCells, List<TableCell> valueCells) {
		int col = 0;
		for (TableCell variableCell : variableCells) {
			TableCell valueCell = valueCells.get(col++);
			String header = variableCell.getValue();
			String value = valueCell.getValue();
			name = name.replace("<" + header + ">", value);
		}
		return name;
	}

	private PickleLocation pickleStepLocation(Step step) {
		return new PickleLocation(step.getLocation().getLine(), step.getLocation().getColumn()
				+ (step.getKeyword() != null ? SymbolCounter.countSymbols(step.getKeyword()) : 0));
	}

	private PickleLocation pickleLocation(Location location) {
		return new PickleLocation(location.getLine(), location.getColumn());
	}

	private List<PickleTag> pickleTags(List<Tag> tags) {
		List<PickleTag> result = new ArrayList<>();
		for (Tag tag : tags) {
			result.add(pickleTag(tag));
		}
		return result;
	}

	private PickleTag pickleTag(Tag tag) {
		return new PickleTag(pickleLocation(tag.getLocation()), tag.getName());
	}
}
