package com.buschmais.jqassistant.plugin.common.test;

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

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.*;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
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
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Abstract base class for analysis tests.
 */
public abstract class AbstractPluginIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginIT.class);

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    protected @interface TestStore {

        boolean reset() default true;

        Type type() default Type.FILE;

        enum Type {
            FILE, MEMORY, REMOTE;
        }

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

        Method getTestMethod() {
            return testMethod;
        }
    }

    protected static final String ARTIFACT_ID = "artifact";

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

    /**
     * The store.
     */
    protected Store store;

    protected Analyzer analyzer;

    protected ReportContext reportContext;

    protected InMemoryReportPlugin reportPlugin;

    private RulePluginRepository rulePluginRepository;
    private ModelPluginRepository modelPluginRepository;
    private ScannerPluginRepository scannerPluginRepository;
    private ScopePluginRepository scopePluginRepository;
    private ReportPluginRepository reportPluginRepository;
    private RuleParserPluginRepository ruleParserPluginRepository;
    private RuleInterpreterPluginRepository ruleInterpreterPluginRepository;

    @Before
    public void configurePlugins() throws PluginRepositoryException, com.buschmais.jqassistant.core.analysis.api.rule.RuleException, IOException {
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(AbstractPluginIT.class.getClassLoader());
        modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader);
        scopePluginRepository = new ScopePluginRepositoryImpl(pluginConfigurationReader);
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        reportPluginRepository = new ReportPluginRepositoryImpl(pluginConfigurationReader);
        ruleParserPluginRepository = new RuleParserPluginRepositoryImpl(pluginConfigurationReader);
        ruleInterpreterPluginRepository = new RuleInterpreterPluginRepositoryImpl(pluginConfigurationReader);

        File selectedDirectory = new File(getClassesDirectory(this.getClass()), "rules");
        // read rules from rules directory
        List<RuleSource> sources = new LinkedList<>();
        if (selectedDirectory.exists()) {
            sources.addAll(FileRuleSource.getRuleSources(selectedDirectory));
        }
        // read rules from plugins
        sources.addAll(rulePluginRepository.getRuleSources());
        Collection<RuleParserPlugin> ruleParserPlugins = ruleParserPluginRepository.getRuleParserPlugins(RuleConfiguration.DEFAULT);
        RuleParser ruleParser = new RuleParser(ruleParserPlugins);
        ruleSet = ruleParser.parse(sources);
    }

    @Before
    public void initializeAnalyzer() throws PluginRepositoryException {
        File outputDirectory = new File("target/jqassistant");
        outputDirectory.mkdirs();
        this.reportContext = new ReportContextImpl(outputDirectory);
        reportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(Collections.emptyMap()));
        AnalyzerConfiguration configuration = new AnalyzerConfiguration();
        analyzer = new AnalyzerImpl(configuration, store, getRuleInterpreterPlugins(), reportPlugin, LOGGER);
    }

    protected Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins() throws PluginRepositoryException {
        return ruleInterpreterPluginRepository.getRuleInterpreterPlugins(Collections.emptyMap());
    }

    /**
     * Initializes and resets the store.
     */
    @Before
    public void startStore() throws URISyntaxException {
        TestStore testStore = testContextRule.getTestMethod().getAnnotation(TestStore.class);
        TestStore.Type type = getTestStoreType(testStore);
        Properties properties = new Properties();
        URI uri;
        switch (type) {
        case FILE:
            String fileName = "target/jqassistant/" + this.getClass().getSimpleName() + "-" + testContextRule.getTestMethod().getName();
            uri = new File(fileName).toURI();
            break;
        case MEMORY:
            uri = new URI("memory:///");
            break;
        case REMOTE:
            uri = new URI("bolt://localhost:7687");
            properties.put("neo4j.remote.statement.log.level", "info");
            break;
        default:
            throw new AssertionError("Test store type not supported: " + type);
        }
        /*
         * You might break IT of depending jQAssistant plugins if you change the
         * location of the used database. Oliver B. Fischer, 2017-06-10
         */
        StoreConfiguration configuration = StoreConfiguration.builder().uri(uri).username("neo4j").password("admin").properties(properties).build();
        store = StoreFactory.getStore(configuration);
        store.start(getDescriptorTypes());
        if (testStore == null || testStore.reset()) {
            store.reset();
        }
    }

    /**
     * Determines the type of the test store to use.
     *
     * @param testStore
     *            The {@link TestStore} annotation (if present).
     * @return The {@link TestStore.Type}.
     */
    protected TestStore.Type getTestStoreType(TestStore testStore) {
        return testStore != null ? testStore.type() : TestStore.Type.FILE;
    }

    /**
     * Stops the store.
     */
    @After
    public void stopStore() {
        if (store != null) {
            store.stop();
        }
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
     * Determines the directory a class is located in (e.g. target/test-classes).
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
     * Deletes the node representing the test class and all its relationships from
     * the store.
     */
    protected void removeTestClass() {
        store.beginTransaction();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("className", this.getClass().getName());
        store.executeQuery("MATCH (t:Type)-[r]-() WHERE t.fqn={className} DELETE r", parameters).close();
        store.executeQuery("MATCH (t:Type) WHERE t.fqn={className} DELETE t", parameters).close();
        store.commitTransaction();
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractPluginIT.TestResult} .
     *
     * @param query
     *            The query.
     * @return The {@link AbstractPluginIT.TestResult}.
     */
    protected TestResult query(String query) {
        return query(query, Collections.<String, Object> emptyMap());
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractPluginIT.TestResult} .
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
    protected com.buschmais.jqassistant.core.analysis.api.Result<Concept> applyConcept(String id) throws RuleException {
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
    protected com.buschmais.jqassistant.core.analysis.api.Result<Concept> applyConcept(String id, Map<String, String> parameters) throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder().addConceptId(id).build();
        Concept concept = ruleSet.getConceptBucket().getById(id);
        assertNotNull("The requested concept cannot be found: " + id, concept);
        analyzer.execute(ruleSet, ruleSelection, parameters);
        return reportPlugin.getConceptResults().get(id);
    }

    /**
     * Validates the constraint identified by id.
     *
     * @param id
     *            The id.
     * @return The result.
     */
    protected com.buschmais.jqassistant.core.analysis.api.Result<Constraint> validateConstraint(String id) throws RuleException {
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
    protected com.buschmais.jqassistant.core.analysis.api.Result<Constraint> validateConstraint(String id, Map<String, String> parameters)
            throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder().addConstraintId(id).build();
        Constraint constraint = ruleSet.getConstraintBucket().getById(id);
        assertNotNull("The requested constraint cannot be found: " + id, constraint);
        analyzer.execute(ruleSet, ruleSelection, parameters);
        return reportPlugin.getConstraintResults().get(id);
    }

    /**
     * Executes the group identified by id.
     *
     * @param id
     *            The id.
     */
    protected void executeGroup(String id) throws RuleException {
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
    protected void executeGroup(String id, Map<String, String> parameters) throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder().addGroupId(id).build();
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
        return getReportPlugins(reportContext, properties);
    }

    protected Map<String, ReportPlugin> getReportPlugins(ReportContext reportContext, Map<String, Object> properties) {
        try {
            return reportPluginRepository.getReportPlugins(reportContext, properties);
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
