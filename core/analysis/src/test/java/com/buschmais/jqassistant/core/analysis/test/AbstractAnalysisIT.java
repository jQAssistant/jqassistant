package com.buschmais.jqassistant.core.analysis.test;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.CatalogReaderImpl;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import com.buschmais.jqassistant.core.model.api.rules.Group;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.RuleSet;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.scanner.test.AbstractScannerIT;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.transform.Source;
import java.util.List;

/**
 * Abstract base class for analysis tests.
 */
public class AbstractAnalysisIT extends AbstractScannerIT {

    protected static RuleSet ruleSet;

    protected Analyzer analyzer;

    protected InMemoryReportWriter reportWriter;

    @BeforeClass
    public static void readRules() {
        CatalogReader catalogReader = new CatalogReaderImpl();
        List<Source> sources = catalogReader.readCatalogs();
        RulesReader rulesReader = new RulesReaderImpl();
        ruleSet = rulesReader.read(sources);
    }

    @Before
    public void initializeAnalyzer() {
        reportWriter = new InMemoryReportWriter();
        analyzer = new AnalyzerImpl(store, reportWriter);
    }

    /**
     * Applies the concept identified by id.
     *
     * @param id The id.
     * @throws ReportWriterException If the report writer reports an error.
     */
    protected void applyConcept(String id) throws ReportWriterException {
        Concept concept = ruleSet.getConcepts().get(id);
        Assert.assertNotNull("The concept must not be null", concept);
        analyzer.applyConcept(concept);
    }

    /**
     * Executes the group identified by id.
     *
     * @param id The id.
     * @throws ReportWriterException If the report writer reports an error.
     */
    protected void executeGroup(String id) throws ReportWriterException {
        Group group = ruleSet.getGroups().get(id);
        Assert.assertNotNull("The group must not be null", group);
        analyzer.executeGroup(group);
    }

}
