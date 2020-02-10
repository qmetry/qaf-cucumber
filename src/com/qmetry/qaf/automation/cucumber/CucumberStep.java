package com.qmetry.qaf.automation.cucumber;

import static com.qmetry.qaf.automation.core.ConfigurationManager.getBundle;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.ConfigurationMap;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.text.StrSubstitutor;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qmetry.qaf.automation.core.AutomationError;
import com.qmetry.qaf.automation.gson.GsonDeserializerObjectWrapper;
import com.qmetry.qaf.automation.gson.ObjectWrapper;
import com.qmetry.qaf.automation.step.BDDStepMatcherFactory.GherkinStepMatcher;
import com.qmetry.qaf.automation.step.BaseTestStep;
import com.qmetry.qaf.automation.step.QAFTestStepArgumentFormatter;
import com.qmetry.qaf.automation.step.QAFTestStepArgumentFormatterImpl;
import com.qmetry.qaf.automation.step.TestStep;
import com.qmetry.qaf.automation.util.ClassUtil;

import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.cucumberexpressions.ExpressionFactory;
import io.cucumber.cucumberexpressions.ParameterTypeRegistry;
import io.cucumber.cucumberexpressions.RegularExpression;
import io.cucumber.datatable.DataTable;

/**
 * This is a wrapper class for cucumber step implementation. This class will be
 * used when QAF BDD factory is used as runner.
 * 
 * @author chirag.jayswal
 *
 */
public class CucumberStep extends BaseTestStep {
	private static final ExpressionFactory FACTORY = new ExpressionFactory(new ParameterTypeRegistry(Locale.ENGLISH));

	private StepDefinition s;

	public CucumberStep(StepDefinition s) {
		super(s.toString(), getPattern(s));
		this.s = s;

		if (FACTORY.createExpression(s.getPattern()) instanceof RegularExpression) {
			setStepMatcher(new GherkinStepMatcher());
		} else {
			setStepMatcher(new CucumberStepMatcher());
		}
	}

	private static String getPattern(StepDefinition s) {
		String pattern = s.getPattern();
		List<ParameterInfo> params = s.parameterInfos();
		if (null != params && !params.isEmpty()) {
			Type last = params.get(params.size() - 1).getType();
			if (ClassUtil.isAssignableFrom(last, DataTable.class)) {
				pattern = pattern + "{string}";
			}
		}
		// pattern = processEscapes(pattern);
		return pattern;
	}

	@Override
	public String getSignature() {
		return s.getLocation();
	}

	@Override
	public TestStep clone() {
		CucumberStep cloneObj = new CucumberStep(s);
		if (null != actualArgs) {
			cloneObj.actualArgs = actualArgs.clone();
		}
		return cloneObj;
	}

	@Override
	protected Object doExecute() {
		try {
			Object[] args = processArgs(s.parameterInfos(), actualArgs);

			s.execute(args);
		} catch (CucumberInvocationTargetException cie) {
			// e.printStackTrace();
			AutomationError ae = new AutomationError(cie.getInvocationTargetExceptionCause() + "-" + s.getLocation(),
					cie.getInvocationTargetExceptionCause());
			ae.setStackTrace(cie.getInvocationTargetExceptionCause().getStackTrace());

			throw ae;
		} catch (Exception e) {
			// e.printStackTrace();
			throw new AutomationError(s.getLocation(), e.getCause());
		}
		return null;
	}

	protected Object[] processArgs(List<ParameterInfo> list, Object... objects) {
		int noOfParams = list.size();
		if (noOfParams == 0) {
			return null;
		}
		Object[] params = new Object[noOfParams];
		Map<String, Object> context = getStepExecutionTracker().getContext();

		try {
			if ((noOfParams == (objects.length - 1)) && list.get(noOfParams - 1).getType().getClass().isArray()) {
				// case of optional arguments!...
				System.arraycopy(objects, 0, params, 0, objects.length);
				params[noOfParams - 1] = "[]";
			} else {
				System.arraycopy(objects, 0, params, 0, noOfParams);
			}
		} catch (Exception e) {
			throw new RuntimeException("Wrong number of parameters, Expected " + noOfParams
					+ " parameters but Actual is " + (objects == null ? "0" : objects.length));
		}

		description = StrSubstitutor.replace(description, context);
		description = getBundle().getSubstitutor().replace(description);
		// Annotation[][] paramsAnnotations = list.getParameterAnnotations();
		// context.put("__method", list);
		QAFTestStepArgumentFormatter<Object> formatter = new QAFTestStepArgumentFormatterImpl();
		for (int i = 0; i < noOfParams; i++) {
			Type paramType = list.get(i).getType();
			context.put("__paramType", paramType);
			context.put("__paramIndex", i);

			if (paramType.getTypeName().endsWith("DataTable")) {
				formatter = new DataTableFormattor();
			}
			params[i] = formatter.format(params[i], context);
			if (!params[i].getClass().isAssignableFrom((Class<?>) paramType)
					&& String.class.isAssignableFrom((Class<?>) paramType)) {
				params[i] = new Gson().toJson(params[i]);
			}

		}
		return params;
	}

	static class DataTableFormattor implements QAFTestStepArgumentFormatter<Object> {
		private static final Gson gson = new GsonBuilder().setDateFormat("dd-MM-yyyy")
				.registerTypeAdapter(ObjectWrapper.class, new GsonDeserializerObjectWrapper(Object.class)).create();

		@Override
		public Object format(Object value, Map<String, Object> context) {
			Class<?> paramType = (Class<?>) context.get("__paramType");

			if ((value instanceof String)) {
				String pstr = (String) value;

				if (pstr.startsWith("${") && pstr.endsWith("}")) {
					String pname = pstr.substring(2, pstr.length() - 1);
					value = context.containsKey(pstr) ? context.get(pstr)
							: context.containsKey(pname) ? context.get(pname)
									: getBundle().containsKey(pstr) ? getObject(pstr, paramType)
											: getPropValue(pname, paramType);
				} else if (pstr.indexOf("$") >= 0) {
					pstr = StrSubstitutor.replace(pstr, context);
					value = getBundle().getSubstitutor().replace(pstr);
				}
			}
			String strVal;
			if (value instanceof String) {
				strVal = String.valueOf(value);
			} else {
				strVal = gson.toJson(value);
			}

			strVal = getBundle().getSubstitutor().replace(strVal);
			strVal = StrSubstitutor.replace(strVal, context);

			// prevent gson from expressing integers as floats
			ObjectWrapper w = gson.fromJson(strVal, ObjectWrapper.class);
			Object obj = w.getObject();
			List<List<String>> list = createListFromVal(obj);
			DataTable table = DataTable.create(list);
			return table;
		}

		@SuppressWarnings("unchecked")
		private List<List<String>> createListFromVal(Object value) {
			List<List<String>> valueToReturn = new ArrayList<List<String>>();
			List<Object> list = (List<Object>) value;
			if (!list.isEmpty() && list.get(0) instanceof Map) {
				Set<String> row1 = ((Map<String, Object>) list.get(0)).keySet();
				valueToReturn.add(new LinkedList<>(row1));
				for (Object o : list) {
					List<String> row = new LinkedList<String>();
					for (Object item : ((Map<String, Object>) o).values()) {
						row.add(item.toString());
					}
					valueToReturn.add(row);
				}

			} else {
				for (Object item : list) {
					valueToReturn.add(Arrays.asList(item.toString()));
				}
			}

			return valueToReturn;
		}

		private Object getPropValue(String pname, Class<?> paramType) {
			Object o = getBundle().subset(pname);
			if (o instanceof HierarchicalConfiguration && ((HierarchicalConfiguration) o).getRoot().getValue() == null
					&& ((HierarchicalConfiguration) o).getRoot().getChildrenCount() > 0) {
				return new ConfigurationMap(getBundle().subset(pname));
			}
			return getObject(pname, paramType);
		}

		private Object getObject(String key, Class<?> paramType) {
			Object o = getBundle().getProperty(key);
			if (null != o && o.toString().indexOf("${") >= 0) {
				String ref = o.toString();
				if (ref.startsWith("${") && ref.toString().endsWith("}")) {
					String pname = ref.substring(2, ref.length() - 1);
					return getPropValue(pname, paramType);
				}
				return getBundle().getSubstitutor().replace(ref);
			}
			if (null == o || o.getClass().isAssignableFrom(paramType)) {
				return o;
			}
			if (paramType.isArray()) {
				return getBundle().getList(key).toArray();
			}
			if (o instanceof List) {
				return ((List<?>) o).get(0);
			}
			return o;
		}

	}
}
