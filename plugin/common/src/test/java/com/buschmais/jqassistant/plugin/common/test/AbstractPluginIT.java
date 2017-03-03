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
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.*;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;
import com.buschmais.jqassistant.core.rule.api.reader.RuleSetReader;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.CompoundRuleSetReader;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerConfiguration;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.xo.api.Query;

/**
 * Abstract base class for analysis tests.
 */
public abstract class AbstractPluginIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginIT.class);

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
                String methodName = description.getMethodName();

                // Handles method names of parameterized JUnit tests
                // They end with an index as "[0]"
                if (methodName.matches(".*\\[\\d+\\]$")) {
                    methodName = methodName.replaceAll("\\[\\d+\\]", "");
                }

                testMethod = testClass.getDeclaredMethod(methodName);
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

    protected RuleSet ruleSet;

    protected Analyzer analyzer;

    protected InMemoryReportWriter reportWriter;

    private RulePluginRepository rulePluginRepository;
    private ModelPluginRepository modelPluginRepository;
    private ScannerPluginRepository scannerPluginRepository;
    private ScopePluginRepository scopePluginRepository;
    private ReportPluginRepository reportPluginRepository;

    @Before
    public void configurePlugins() throws PluginRepositoryException, RuleException, IOException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(AbstractPluginIT.class.getClassLoader());
        modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        scopePluginRepository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader);

        File selectedDirectory = new File(getClassesDirectory(this.getClass()), "rules");
        // read rules from rules directory
        List<RuleSource> sources = new LinkedList<>();
        if (selectedDirectory.exists()) {
            sources.addAll(FileRuleSource.getRuleSources(selectedDirectory));
        }
        // read rules from plugins
        sources.addAll(rulePluginRepository.getRuleSources());
        RuleSetReader ruleSetReader = new CompoundRuleSetReader();
        RuleSetBuilder ruleSetBuilder = RuleSetBuilder.newInstance();
        ruleSetReader.read(sources, ruleSetBuilder);
        ruleSet = ruleSetBuilder.getRuleSet();
    }

    @Before
    public void initializeAnalyzer() {
        reportWriter = new InMemoryReportWriter(new CompositeReportPlugin(Collections.<String, ReportPlugin> emptyMap()));
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        analyzer = new AnalyzerImpl(configuration, store, reportWriter, LOGGER);
    }

    /**
     * The store.
     */
    protected Store store;

    /**
     * Initializes and resets the store.
     */
    @Before
    public void startStore() throws PluginRepositoryException, URISyntaxException {
        String fileName = "target/jqassistant/" + this.getClass().getSimpleName() + "-" + testContextRule.getTestMethod().getName();
        URI uri = new File(fileName).toURI();
        //URI uri = new URI("bolt://localhost:7687");
        //URI uri = new URI("memory:///");
        Properties properties = new Properties();
        properties.put("neo4j.remote.statement.log", "none");
        StoreConfiguration configuration = StoreConfiguration.builder().uri(uri).username("neo4j").password("admin").properties(properties).build();
        store = StoreFactory.getStore(configuration);
        store.start(getDescriptorTypes());
        TestStore testStore = testContextRule.getTestMethod().getAnnotation(TestStore.class);
        if (testStore == null || testStore.reset()) {
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
        return getScanner(Collections.<String, Object> emptyMap());
    }

    /**
     * Return an initialized scanner instance.
     *
     * @param properties
     *            The properties to be used to configure the plugins.
     * @return The artifact scanner instance.
     */
    protected Scanner getScanner(Map<String, Object> properties) {
        ScannerContext scannerContext = new ScannerContextImpl(store);
        Map<String, ScannerPlugin<?, ?>> scannerPlugins = getScannerPlugins(scannerContext, properties);
        return new ScannerImpl(getScannerConfiguration(), scannerContext, scannerPlugins, scopePluginRepository.getScopes());
    }

    /**
     * Return the scanner configuration for the test.
     *
     * @return The scanner configuration.
     */
    protected ScannerConfiguration getScannerConfiguration() {
        return new ScannerConfiguration();
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
        File directory = ClasspathResource.getFile(rootClass, "/");
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
     */
    protected com.buschmais.jqassistant.core.analysis.api.Result<Concept> applyConcept(String id) throws Exception {
        return applyConcept(id, Collections.<String, String> emptyMap());
    }

    /**
     * Applies the concept identified by id.
     *
     * @param id
     *            The id.
     * @param parameters
     *            The rule parameters.
     * @return The result.
     */
    protected com.buschmais.jqassistant.core.analysis.api.Result<Concept> applyConcept(String id, Map<String, String> parameters) throws Exception {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConceptId(id).get();
        Concept concept = ruleSet.getConceptBucket().getById(id);
        assertNotNull("The requested concept cannot be found: " + id, concept);
        analyzer.execute(ruleSet, ruleSelection, parameters);
        return reportWriter.getConceptResults().get(id);
    }

    /**
     * Validates the constraint identified by id.
     *
     * @param id
     *            The id.
     * @return The result.
     */
    protected com.buschmais.jqassistant.core.analysis.api.Result<Constraint> validateConstraint(String id) throws Exception {
        return validateConstraint(id, Collections.<String, String> emptyMap());
    }

    /**
     * Validates the constraint identified by id.
     *
     * @param id
     *            The id.
     * @param parameters
     *            The rule parameters.
     * @return The result.
     */
    protected com.buschmais.jqassistant.core.analysis.api.Result<Constraint> validateConstraint(String id, Map<String, String> parameters) throws Exception {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConstraintId(id).get();
        Constraint constraint = ruleSet.getConstraintBucket().getById(id);
        assertNotNull("The requested constraint cannot be found: " + id, constraint);
        analyzer.execute(ruleSet, ruleSelection, parameters);
        return reportWriter.getConstraintResults().get(id);
    }

    /**
     * Executes the group identified by id.
     *
     * @param id
     *            The id.
     */
    protected void executeGroup(String id) throws RuleExecutorException, NoGroupException {
        executeGroup(id, Collections.<String, String> emptyMap());
    }

    /**
     * Executes the group identified by id.
     *
     * @param id
     *            The id.
     * @param parameters
     *            The rule parameters.
     */
    protected void executeGroup(String id, Map<String, String> parameters) throws RuleExecutorException, NoGroupException {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addGroupId(id).get();
        Group group = ruleSet.getGroupsBucket().getById(id);
        assertNotNull("The request group cannot be found: " + id, group);
        analyzer.execute(ruleSet, ruleSelection, parameters);
    }

    private List<Class<?>> getDescriptorTypes() {
        try {
            return modelPluginRepository.getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get descriptor types.", e);
        }
    }

    private Map<String, ScannerPlugin<?, ?>> getScannerPlugins(ScannerContext scannerContext, Map<String, Object> properties) {
        try {
            return scannerPluginRepository.getScannerPlugins(scannerContext, properties);
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get scanner plugins.", e);
        }
    }

    protected Map<String, ReportPlugin> getReportPlugins(Map<String, Object> properties) {
        try {
            return reportPluginRepository.getReportPlugins(properties);
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
