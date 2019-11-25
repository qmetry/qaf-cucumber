package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;
import static com.qmetry.qaf.automation.data.MetaDataScanner.applyMetafilter;
import static com.qmetry.qaf.automation.data.MetaDataScanner.hasDP;
import static com.qmetry.qaf.automation.util.ClassUtil.setField;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.json.JSONObject;

import com.qmetry.qaf.automation.testng.dataprovider.QAFInetrceptableDataProvider;
import com.qmetry.qaf.automation.util.StringUtil;

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
/**
 * 
 * @author chirag.jayswal
 *
 */
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
				Map<String, Object> metadata = getMetaData(featureTags, ((Scenario) scenarioDefinition).getTags(),
						pickleLocation(scenarioDefinition.getLocation()));
				if (applyMetafilter(metadata)) {
					if (hasDP(metadata)) {
						ScenarioOutline outline = new ScenarioOutline(((Scenario) scenarioDefinition).getTags(),
								scenarioDefinition.getLocation(), scenarioDefinition.getKeyword(),
								scenarioDefinition.getName(), scenarioDefinition.getDescription(),
								scenarioDefinition.getSteps(), null);
						compileScenarioOutline(pickles, backgroundSteps, outline, featureTags, language, metadata);
					} else {
						compileScenario(pickles, backgroundSteps, (Scenario) scenarioDefinition, featureTags, language,
								metadata);
					}
				}
			} else {
				Map<String, Object> metadata = getMetaData(featureTags,
						((ScenarioOutline) scenarioDefinition).getTags(),
						pickleLocation(scenarioDefinition.getLocation()));
				if (applyMetafilter(metadata)) {
					compileScenarioOutline(pickles, backgroundSteps, (ScenarioOutline) scenarioDefinition, featureTags,
							language, metadata);
				}
			}
		}
		return pickles;
	}

	private void compileScenario(List<Pickle> pickles, List<PickleStep> backgroundSteps, Scenario scenario,
			List<Tag> featureTags, String language, Map<String, Object> metadata) {
		List<PickleStep> steps = new ArrayList<>();
		if (!scenario.getSteps().isEmpty())
			steps.addAll(backgroundSteps);

		List<Tag> scenarioTags = new ArrayList<>();
		scenarioTags.addAll(featureTags);
		scenarioTags.addAll(scenario.getTags());

		steps.addAll(pickleSteps(scenario));

		Pickle pickle = new Bdd2Pickle(scenario.getName(), language, steps, pickleTags(scenarioTags),
				singletonList(pickleLocation(scenario.getLocation())), metadata);
		pickles.add(pickle);
	}

	private void compileScenarioOutline(List<Pickle> pickles, List<PickleStep> backgroundSteps,
			ScenarioOutline scenarioOutline, List<Tag> featureTags, String language, Map<String, Object> metadata) {
		List<Tag> scenariotags = new ArrayList<>();
		scenariotags.addAll(featureTags);
		scenariotags.addAll(scenarioOutline.getTags());
		List<Examples> examplesToUse = getExamples(metadata, scenarioOutline);
		
		for (final Examples examples : examplesToUse) {
			if (examples.getTableHeader() == null)
				continue;
			List<TableCell> variableCells = examples.getTableHeader().getCells();
			for (final TableRow values : examples.getTableBody()) {
				List<TableCell> valueCells = values.getCells();

				List<PickleStep> steps = new ArrayList<>();
				if (!scenarioOutline.getSteps().isEmpty())
					steps.addAll(backgroundSteps);


				for (Step scenarioOutlineStep : scenarioOutline.getSteps()) {
					String stepText = interpolate(scenarioOutlineStep.getText(), variableCells, valueCells);

					PickleStep pickleStep = new PickleStep(stepText,
							createPickleArguments(scenarioOutlineStep.getArgument(), variableCells, valueCells),
							asList(pickleLocation(values.getLocation()), pickleStepLocation(scenarioOutlineStep)));
					steps.add(pickleStep);
				}

				List<Tag> tags = new ArrayList<>(scenariotags);
				tags.addAll(examples.getTags());
				
				Pickle pickle = new Bdd2Pickle(interpolate(scenarioOutline.getName(), variableCells, valueCells),
						language, steps, pickleTags(tags),
						asList(pickleLocation(values.getLocation()), pickleLocation(scenarioOutline.getLocation())),
						examples.getTableHeader().getCells(), valueCells,  getMetaData(metadata, examples.getTags()));

				pickles.add(pickle);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private List<Examples> getExamples(Map<String, Object> metadata, ScenarioOutline scenarioOutline) {
		if (!hasDP(metadata)) {
			return scenarioOutline.getExamples();
		}

		List<Examples> listToReturn = new ArrayList<Examples>();
		List<Object[]> externalData = null;
		Location location = scenarioOutline.getLocation();

		try {
			externalData = Arrays.asList(QAFInetrceptableDataProvider.getData(metadata));
		} catch (Exception e) {
			if ("No data provider found".equalsIgnoreCase(e.getMessage())) {
				return scenarioOutline.getExamples();
			}
			// throw new AutomationError(e);
		}

		if (null == externalData) {
			return scenarioOutline.getExamples();
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
		setField("examples", scenarioOutline, listToReturn);

		return listToReturn;
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
		Map<String, Object> row = new HashMap<String, Object>();
		for (TableCell variableCell : variableCells) {
			TableCell valueCell = valueCells.get(col++);
			String header = variableCell.getValue();
			String value = valueCell.getValue();
			name = name.replace("<" + header + ">", value);
			name = name.replace("${" + header + "}", value);
			row.put(header, value);
		}
		name = name.replace("\"${args[0]}\"", JSONObject.quote(JSONObject.valueToString(row)));
		name = name.replace("${args[0]}", JSONObject.valueToString(row) );
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


	private Map<String, Object> getMetaData(List<Tag> featureTags, List<Tag> tags, PickleLocation pickleLocation) {
		Map<String, Object> metaData = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		metaData.put("line", pickleLocation.getLine());
		addMetaData(metaData, featureTags);
		addMetaData(metaData, tags);
		return metaData;
	}

	private void addMetaData(Map<String, Object> metaData, List<Tag> tags) {
		tags.stream().filter(tag -> tag.getName().contains(":")).forEach(tag -> {
			String[] kv = tag.getName().substring(1).split(":", 2);
			metaData.put(kv[0], StringUtil.toObject(getBundle().getSubstitutor().replace(kv[1])));
		});
		@SuppressWarnings("unchecked")
		List<String> groups = (List<String>) metaData.getOrDefault("groups", new ArrayList<String>());
		// List<String> groups =
		tags.stream().filter(tag -> !tag.getName().contains(":")).forEach(tag -> {
			groups.add(tag.getName().substring(1));
		});
		metaData.put("groups", groups);
	}
	
	private Map<String, Object> getMetaData(Map<String, Object> metaData, List<Tag> tags) {
		Map<String, Object> metaDataToReturn = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		metaDataToReturn.putAll(metaData);
		addMetaData(metaDataToReturn, tags);
		return metaData;
	}

}
