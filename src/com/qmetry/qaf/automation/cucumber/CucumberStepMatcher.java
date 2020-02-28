/**
 * 
 */
package com.qmetry.qaf.automation.cucumber;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.qmetry.qaf.automation.step.BDDStepMatcherFactory.DefaultBDDStepMatcher;

import io.cucumber.core.exception.CucumberException;

/**
 * @author chirag.jayswal
 *
 */
public class CucumberStepMatcher extends DefaultBDDStepMatcher {
	private static final Pattern PARAMETER_PATTERN = Pattern.compile("(\\\\\\\\)?\\{([^}]*)\\}");
	private static final Pattern OPTIONAL_PATTERN = Pattern.compile("(\\\\)?\\(([^)]+)\\)");
	private static final Pattern ALTERNATIVE_NON_WHITESPACE_TEXT_REGEXP = Pattern.compile("([^\\s^/]+)((/[^\\s^/]+)+)");
	private static final String ESCAPE = "\\";
	private static final String PARAMETER_TYPES_CANNOT_BE_ALTERNATIVE = "Parameter types cannot be alternative: ";
	private static final String PARAMETER_TYPES_CANNOT_BE_OPTIONAL = "Parameter types cannot be optional: ";

	@Override
	public boolean matches(String stepDescription, String stepCall, Map<String, Object> context) {
		stepDescription = processOptional(stepDescription);
		stepDescription = processAlternation(stepDescription);
		return super.matches(stepDescription, stepCall, context);
	}

	@Override
	public List<String[]> getArgsFromCall(String stepDescription, String stepCall, Map<String, Object> context) {
		stepDescription = processOptional(stepDescription);
		stepDescription = processAlternation(stepDescription);
		return super.getArgsFromCall(stepDescription, stepCall, context);
	}

	private static String processOptional(String expression) {
		Matcher matcher = OPTIONAL_PATTERN.matcher(expression);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			// look for double-escaped parentheses
			String parameterPart = matcher.group(2);
			String grp1=matcher.group(1);
			if (!ESCAPE.equals(grp1)) {
				checkNotParameterType(parameterPart, PARAMETER_TYPES_CANNOT_BE_OPTIONAL);
				matcher.appendReplacement(sb, "(?:" + parameterPart + ")?");
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private static String processAlternation(String expression) {
		Matcher matcher = ALTERNATIVE_NON_WHITESPACE_TEXT_REGEXP.matcher(expression);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			// replace \/ with /
			// replace / with |
			String replacement = matcher.group(0).replace('/', '|').replaceAll("\\\\\\|", "/");

			if (replacement.contains("|")) {
				// Make sure the alternative parts don't contain parameter types
				for (String part : replacement.split("\\|")) {
					checkNotParameterType(part, PARAMETER_TYPES_CANNOT_BE_ALTERNATIVE);
				}
				matcher.appendReplacement(sb, "(?:" + replacement + ")");
			} else {
				// All / were escaped
				matcher.appendReplacement(sb, replacement);
			}
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	private static void checkNotParameterType(String s, String message) {
		Matcher matcher = PARAMETER_PATTERN.matcher(s);
		if (matcher.find()) {
			throw new CucumberException(message + s);
		}
	}
}
