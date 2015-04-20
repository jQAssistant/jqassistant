package com.buschmais.jqassistant.plugin.common.test;

import static com.buschmais.xo.api.Query.Result.CompositeRowObject;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.*;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.plugin.common.test.matcher.TestConsole;
import com.buschmais.xo.api.Query;

/**
 * Abstract base class for analysis tests.
 */
public abstract class AbstractPluginIT {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected @interface TestStore {
        boolean reset() default true;
    }

    /**
     * A rule implementation
     */
    private class TestContextRule extends TestWatcher {

        private Method testMethod;

        @Override
        protected void starting(Description description) {
            Class<?> testClass = description.getTestClass();
            try {
                testMethod = testClass.getDeclaredMethod(description.getMethodName());
            } catch (NoSuchMethodException e) {
                Assert.fail(e.getMessage());
            }
        }

        public Method getTestMethod() {
            return testMethod;
        }
    }

    public static final String ARTIFACT_ID = "artifact";

    @Rule
    public TestContextRule testContextRule = new TestContextRule();

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
         * Return a column identified by its name.
         * 
         * @param <T>
         *            The expected type.
         * @return All columns.
         */
        public <T> List<T> getColumn(String name) {
            return (List<T>) columns.get(name);
        }
    }

    protected static RuleSet ruleSet;

    protected Analyzer analyzer;

    protected InMemoryReportWriter reportWriter;

    private RulePluginRepository rulePluginRepository;
    private ModelPluginRepository modelPluginRepository;
    private ScannerPluginRepository scannerPluginRepository;
    private ScopePluginRepository scopePluginRepository;
    private ReportPluginRepository reportPluginRepository;

    @Before
    public void readRules() throws PluginRepositoryException, RuleException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(AbstractPluginIT.class.getClassLoader());
        modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        scopePluginRepository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader);
        List<RuleSource> sources = rulePluginRepository.getRuleSources();
        RuleSetReader ruleSetReader = new CompoundRuleSetReader();
        ruleSet = ruleSetReader.read(sources);
    }

    /**
     * Provides the properties to be passed to scanner plugins. May be
     * overwritten by test classes.
     * 
     * @return The scanner properties.
     */
    protected Map<String, Object> getScannerProperties() {
        return Collections.emptyMap();
    }

    /**
     * Provides the properties to be passed to report plugins. May be
     * overwritten by test classes.
     *
     * @return The report properties.
     */
    protected Map<String, Object> getReportProperties() {
        return Collections.emptyMap();
    }

    @Before
    public void initializeAnalyzer() {
        reportWriter = new InMemoryReportWriter();
        analyzer = new AnalyzerImpl(store, reportWriter, new TestConsole());
    }

    /**
     * The store.
     */
    protected Store store;

    /**
     * Initializes and resets the store.
     */
    @Before
    public void startStore() throws PluginRepositoryException {
        store = new EmbeddedGraphStore("target/jqassistant/" + this.getClass().getSimpleName() + "-" + testContextRule.getTestMethod().getName());
        store.start(getDescriptorTypes());
        TestStore testStore = testContextRule.getTestMethod().getAnnotation(TestStore.class);
        boolean resetStore = true;
        if (testStore != null) {
            resetStore = testStore.reset();
        }
        if (resetStore) {
            store.reset();
        }
    }

    /**
     * Stops the store.
     */
    @After
    public void stopStore() {
        store.stop();
    }

    /**
     * Return an initialized scanner instance.
     *
     * @return The artifact scanner instance.
     */
    protected Scanner getScanner() {
        return new ScannerImpl(store, getScannerPlugins(), scopePluginRepository.getScopes());
    }

    /**
     * Determines the directory a class is located in (e.g.
     * target/test-classes).
     * 
     * @param rootClass
     *            The class.
     * @return The directory.
     */
    protected File getClassesDirectory(Class<?> rootClass) {
        // Determine test classes directory.
        URL resource = rootClass.getResource("/");
        String file = resource.getFile();
        File directory = new File(file);
        assertTrue("Expected a directory.", directory.isDirectory());
        return directory;
    }

    /**
     * Deletes the node representing the test class and all its relationships
     * from the store.
     * 
     * @throws IOException
     *             If an error occurs.
     */
    protected void removeTestClass() throws IOException {
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("className", this.getClass().getName());
        store.executeQuery("MATCH (t:Type)-[r]-() WHERE t.fqn={className} DELETE r", parameters).close();
        store.executeQuery("MATCH (t:Type) WHERE t.fqn={className} DELETE t", parameters).close();
        store.commitTransaction();
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractPluginIT.TestResult}
     * .
     * 
     * @param query
     *            The query.
     * @return The {@link AbstractPluginIT.TestResult}.
     */
    protected TestResult query(String query) {
        return query(query, Collections.<String, Object> emptyMap());
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractPluginIT.TestResult}
     * .
     * 
     * @param query
     *            The query.
     * @param parameters
     *            The query parameters.
     * @return The {@link AbstractPluginIT.TestResult}.
     */
    protected TestResult query(String query, Map<String, Object> parameters) {
        Query.Result<CompositeRowObject> compositeRowObjects = store.executeQuery(query, parameters);
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, List<Object>> columns = new HashMap<>();
        for (CompositeRowObject rowObject : compositeRowObjects) {
            Map<String, Object> row = new HashMap<>();
            Iterable<String> columnNames = rowObject.getColumns();
            for (String columnName : columnNames) {
                List<Object> columnValues = columns.get(columnName);
                if (columnValues == null) {
                    columnValues = new ArrayList<>();
                    columns.put(columnName, columnValues);
                }
                Object value = rowObject.get(columnName, Object.class);
                row.put(columnName, value);
                columnValues.add(value);
            }
            rows.add(row);
        }
        return new TestResult(rows, columns);
    }

    /**
     * Applies the concept identified by id.
     * 
     * @param id
     *            The id.
     * @return The result.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the analyzer reports an error.
     */
    protected com.buschmais.jqassistant.core.analysis.api.Result<Concept> applyConcept(String id) throws AnalysisException {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConceptId(id).get();
        Concept concept = ruleSet.getConcepts().get(id);
        assertNotNull("The requested concept cannot be found: " + id, concept);
        analyzer.execute(ruleSet, ruleSelection);
        return reportWriter.getConceptResults().get(id);
    }

    /**
     * Validates the constraint identified by id.
     * 
     * @param id
     *            The id.
     * @return The result.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the analyzer reports an error.
     */
    protected com.buschmais.jqassistant.core.analysis.api.Result<Constraint> validateConstraint(String id) throws AnalysisException {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConstraintId(id).get();
        Constraint constraint = ruleSet.getConstraints().get(id);
        assertNotNull("The requested constraint cannot be found: " + id, constraint);
        analyzer.execute(ruleSet, ruleSelection);
        return reportWriter.getConstraintResults().get(id);
    }

    /**
     * Executes the group identified by id.
     * 
     * @param id
     *            The id.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the analyzer reports an error.
     */
    protected void executeGroup(String id) throws AnalysisException {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addGroupId(id).get();
        Group group = ruleSet.getGroups().get(id);
        assertNotNull("The request group cannot be found: " + id, group);
        analyzer.execute(ruleSet, ruleSelection);
    }

    private List<Class<?>> getDescriptorTypes() {
        try {
            return modelPluginRepository.getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get descriptor types.", e);
        }
    }

    private List<ScannerPlugin<?, ?>> getScannerPlugins() {
        try {
            return scannerPluginRepository.getScannerPlugins(getScannerProperties());
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get scanner plugins.", e);
        }
    }

    protected List<ReportPlugin> getReportPlugins() {
        try {
            return reportPluginRepository.getReportPlugins(getReportProperties());
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get report plugins.", e);
        }
    }

    protected ScannerPluginRepository getScannerPluginRepository() {
        return scannerPluginRepository;
    }

    protected RulePluginRepository getRulePluginRepository() {
        return rulePluginRepository;
    }
}
