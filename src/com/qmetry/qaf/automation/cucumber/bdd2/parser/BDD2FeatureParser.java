/**
 * 
 */
package com.qmetry.qaf.automation.cucumber.bdd2.parser;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.qmetry.qaf.automation.cucumber.bdd2.model.BDD2Feature;
import com.qmetry.qaf.automation.cucumber.bdd2.model.BDD2PickleWrapper;

import gherkin.AstBuilder;
import gherkin.GherkinDialect;
import gherkin.GherkinDialectProvider;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import io.cucumber.core.gherkin.Feature;
import io.cucumber.core.gherkin.FeatureParser;
import io.cucumber.core.gherkin.FeatureParserException;
import io.cucumber.core.gherkin.Pickle;

/**
 * @author chirag.jayswal
 *
 */
public class BDD2FeatureParser implements FeatureParser {
    private static Feature parseGherkin5(URI path, String source) {
        try {
            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            GherkinDocument gherkinDocument = parser.parse(source, matcher);
            GherkinDialectProvider dialectProvider = new GherkinDialectProvider();
            List<Pickle> pickles = compilePickles(gherkinDocument, dialectProvider, path);
            return new BDD2Feature(gherkinDocument, path, source, pickles);
        } catch (ParserException e) {
            throw new FeatureParserException("Failed to parse resource at: " + path.toString(), e);
        }
    }

    private static List<Pickle> compilePickles(GherkinDocument document, GherkinDialectProvider dialectProvider, URI path) {
        if (document.getFeature() == null) {
            return Collections.emptyList();
        }
        String language = document.getFeature().getLanguage();
        GherkinDialect dialect = dialectProvider.getDialect(language, null);
        return new Bdd2Compiler().compile(document)
            .stream()
            .map(pickle -> new BDD2PickleWrapper(pickle, path, document, dialect))
            .collect(Collectors.toList());
    }


	/* (non-Javadoc)
	 * @see io.cucumber.core.gherkin.FeatureParser#parse(java.net.URI, java.lang.String, java.util.function.Supplier)
	 */
	@Override
	public Feature parse(URI path, String source, Supplier<UUID> idGenerator) {
		// TODO Auto-generated method stub
        return parseGherkin5(path, source);
	}

	/* (non-Javadoc)
	 * @see io.cucumber.core.gherkin.FeatureParser#version()
	 */
	@Override
	public String version() {
		 return "BDD2";
	}

}
