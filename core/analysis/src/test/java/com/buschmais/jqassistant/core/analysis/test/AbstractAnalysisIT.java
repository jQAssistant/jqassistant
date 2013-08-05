package com.buschmais.jqassistant.core.analysis.test;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.CatalogReader;
import com.buschmais.jqassistant.core.analysis.api.RulesReader;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.CatalogReaderImpl;
import com.buschmais.jqassistant.core.analysis.impl.RulesReaderImpl;
import com.buschmais.jqassistant.core.model.api.rules.Concept;
import com.buschmais.jqassistant.core.model.api.rules.RuleSet;
import com.buschmais.jqassistant.report.api.ReportWriterException;
import com.buschmais.jqassistant.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.scanner.test.AbstractScannerIT;
import com.buschmais.jqassistant.store.api.QueryResult;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.transform.Source;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for analysis tests.
 */
public class AbstractAnalysisIT extends AbstractScannerIT {

    /**
     * Represents a test result which allows fetching values by row or columns.
     */
    protected class TestResult {
        private List<Map<String, Object>> rows;
        private Map<String, List<Object>> columns;

        TestResult(List<Map<String, Object>> rows, Map<String, List<Object>> columns) {
            this.rows = rows;
            this.columns = columns;
        }

        /**
         * Return all rows.
         *
         * @return All rows.
         */
        public List<Map<String, Object>> getRows() {
            return rows;
        }

        /**
         * Return all columns identified by their name.
         *
         * @return All columns.
         */
        public Map<String, List<Object>> getColumns() {
            return columns;
        }
    }

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
        Assert.assertNotNull("Concept must not be null", concept);
        analyzer.applyConcept(concept);
    }

    /**
     * Executes a CYPHER query and returns a {@link TestResult}.
     *
     * @param query The query.
     * @return The  {@link TestResult}.
     */
    protected TestResult executeQuery(String query) {
        QueryResult queryResult = store.executeQuery(query);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        Map<String, List<Object>> columns = new HashMap<String, List<Object>>();
        for (String column : queryResult.getColumns()) {
            columns.put(column, new ArrayList<Object>());
        }
        for (QueryResult.Row row : queryResult.getRows()) {
            Map<String, Object> rowData = (Map<String, Object>) row.get();
            rows.add(rowData);
            for (Map.Entry<String, ?> entry : rowData.entrySet()) {
                List<Object> column = columns.get(entry.getKey());
                column.add(entry.getValue());
            }
        }
        return new TestResult(rows, columns);
    }
}
