/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.qmetry.qaf.automation.step.DefaultObjectFactory;
import com.qmetry.qaf.automation.step.JavaStep;
import com.qmetry.qaf.automation.step.TestStep;
import com.qmetry.qaf.automation.step.client.text.BDDDefinitionHelper.ParamType;

import io.cucumber.core.backend.CucumberBackendException;
import io.cucumber.core.backend.CucumberInvocationTargetException;
import io.cucumber.core.backend.Lookup;
import io.cucumber.core.backend.ParameterInfo;
import io.cucumber.core.backend.StepDefinition;
import io.cucumber.core.backend.TypeResolver;
import io.cucumber.datatable.DataTable;

/**
 * This class will be used by QAFBackend when running your BDD with cucumber runner.
 * 
 * @author chirag.jayswal
 */
public class QAFStepDefinition implements StepDefinition {
	TestStep step;
	Lookup lookup;
	String pattern;

	public QAFStepDefinition(TestStep step, Lookup lookup) {
		this.step = (step instanceof JavaStep)? new JavaStepWarapper((JavaStep)step, lookup):step;
		this.lookup = lookup;
		pattern = getPattern(step.getDescription());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * io.cucumber.core.backend.Located#isDefinedAt(java.lang.StackTraceElement)
	 */
	@Override
	public boolean isDefinedAt(StackTraceElement e) {
		// step implementation can be non-java as well
		if (step instanceof JavaStep) {
			Method method = ((JavaStep) step).getMethod();
			return e.getClassName().equals(method.getDeclaringClass().getName())
					&& e.getMethodName().equals(method.getName());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.Located#getLocation()
	 */
	@Override
	public String getLocation() {
		return step.getSignature();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.StepDefinition#execute(java.lang.Object[])
	 */
	@Override
	public void execute(Object[] args) throws CucumberBackendException, CucumberInvocationTargetException {
		processArgs(args);
		step.setActualArgs(args);
		step.execute();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.StepDefinition#parameterInfos()
	 */
	@Override
	public List<ParameterInfo> parameterInfos() {
		List<ParameterInfo> parameterInfos = new ArrayList<ParameterInfo>();
		if (step instanceof JavaStep) {
			for (Parameter param : ((JavaStep) step).getMethod().getParameters()) {
				parameterInfos.add(new ParameterInfoImpl(param.getType()));
			}
		}
		// need to take a look...
		return parameterInfos;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see io.cucumber.core.backend.StepDefinition#getPattern()
	 */
	@Override
	public String getPattern() {
		return pattern;
	}

	private String getPattern(String desce) {
		String pattern = step.getDescription().replaceAll(ParamType.getParamDefRegx(), "{string}");
		if (pattern.endsWith(":{string}") && step instanceof JavaStep) { // exceptional case to allow data table
			Parameter[] types = ((JavaStep) step).getMethod().getParameters();
			if (!types[types.length - 1].getType().isPrimitive())
				return pattern.substring(0, pattern.lastIndexOf("{string}"));
		}
		return pattern;
	}

	class ParameterInfoImpl implements ParameterInfo {

		Type type;

		public ParameterInfoImpl(Type type) {
			this.type = type;
		}

		@Override
		public Type getType() {
			if (type instanceof Class<?> && ((Class<?>) type).isPrimitive()) {
				return type;
			}
			return Object.class;
		}

		@Override
		public boolean isTransposed() {
			return true;
		}

		@Override
		public TypeResolver getTypeResolver() {
			return () -> getType();
		}

	}

	private void processArgs(Object[] args) {
		if (step instanceof JavaStep) {
			Parameter[] params = ((JavaStep) step).getMethod().getParameters();
			for (int i = 0; i < args.length; i++) {
				Object arg = args[i];
				if (arg instanceof DataTable && !(params[i].getType().isAssignableFrom(DataTable.class))) {
					DataTable table = (DataTable) arg;
					args[i] = convertDataTableObject(table, params[i]);
				}
			}
		}
	}

	String convertDataTableObject(DataTable d, Parameter param) {
		List<Map<String, String>> maps = d.transpose().asMaps();

		if (List.class.isAssignableFrom(param.getType())) {
			String ptype = param.getParameterizedType().getTypeName();
			if (ptype.contains("<Map") || ptype.contains("<List") || maps.get(0).keySet().size() > 1) {
				if (ptype.contains("<List")) {
					return JSONObject.valueToString(d.transpose().asLists());
				}
				return JSONObject.valueToString(maps);
			}
			return JSONObject.valueToString(d.transpose().asList());
		}

		if (param.isVarArgs() || param.getType().isArray()) {
			if (maps.get(0).keySet().size() == 1 && param.getType().getComponentType().isPrimitive()) {
				return JSONObject.valueToString(d.transpose().asList());
			}
			return JSONObject.valueToString(maps);
		}
		if (maps.size() == 1) {
			return JSONObject.valueToString(maps.get(0));
		}
		return JSONObject.valueToString(maps);
	}
	
	private class JavaStepWarapper extends JavaStep{
		Lookup lookup;
		public JavaStepWarapper(JavaStep step, Lookup lookup) {
			super(step.getMethod(), step.getName(), step.getDescription());
		}
		@Override
		protected Object getStepProvider() throws Exception {
			Class<?> cls = method.getDeclaringClass();
			try {
				return lookup.getInstance(cls);
			} catch (Exception e) {
				try {
					logger.debug("Unable to crete obect of class["+cls+"] using ["+lookup.getClass()+"]. Using default qaf obect factory as fallback");
					return super.getStepProvider();
				} catch (Exception e1) {
					return new DefaultObjectFactory().getObject(cls);
				}
			}
		}
	}
}
