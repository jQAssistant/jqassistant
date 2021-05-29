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
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RuleConfiguration;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerConfiguration;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreConfiguration;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyMap;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for analysis tests.
 */
public abstract class AbstractPluginIT {

    protected static final String ARTIFACT_ID = "artifact";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginIT.class);

    private static PluginRepositoryImpl pluginRepository;

    private File outputDirectory;

    /**
     * The store.
     */
    protected Store store;
    protected Analyzer analyzer;
    protected ReportContext reportContext;
    protected InMemoryReportPlugin reportPlugin;
    protected RuleSet ruleSet;

    @BeforeAll
    public static void initPluginRepository() {
        ClassLoader pluginClassLoader = AbstractPluginIT.class.getClassLoader();
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();
    }

    @AfterAll
    public static void destroyPluginRepository() {
        if (pluginRepository != null) {
            pluginRepository.destroy();
        }
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) throws Exception {
        initializeRuleSet();
        outputDirectory = new File("target/jqassistant");
        outputDirectory.mkdirs();
        startStore(testInfo);
        initializeAnalyzer();
    }

    /**
     * Stops the store.
     */
    @AfterEach
    public void stopStore() {
        if (store != null) {
            store.stop();
        }
    }

    private void initializeRuleSet() throws RuleException, IOException {
        File selectedDirectory = new File(getClassesDirectory(this.getClass()), "rules");
        // read rules from rules directory
        List<RuleSource> sources = new LinkedList<>();
        if (selectedDirectory.exists()) {
            sources.addAll(FileRuleSource.getRuleSources(selectedDirectory));
        }
        // read rules from plugins
        sources.addAll(pluginRepository.getRulePluginRepository().getRuleSources());
        Collection<RuleParserPlugin> ruleParserPlugins = pluginRepository.getRulePluginRepository().getRuleParserPlugins(RuleConfiguration.DEFAULT);
        RuleParser ruleParser = new RuleParser(ruleParserPlugins);
        ruleSet = ruleParser.parse(sources);
    }

    private void initializeAnalyzer() {
        this.reportContext = new ReportContextImpl(store, outputDirectory);
        this.reportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(getReportPlugins(getReportProperties())));
        AnalyzerConfiguration configuration = getAnalyzerConfiguration();
        analyzer = new AnalyzerImpl(configuration, store, getRuleInterpreterPlugins(), reportPlugin, LOGGER);
    }

    /**
     * Return the properties for the scanner, to be overwritten by sub-classes.
     */
    protected Map<String, Object> getScannerProperties() {
        return emptyMap();
    }

    /**
     * Return the properties for the scanner, to be overwritten by sub-classes.
     */
    protected AnalyzerConfiguration getAnalyzerConfiguration() {
        return new AnalyzerConfiguration();
    }

    /**
     * Return the report properties, to be overwritten by sub-classes.
     */
    protected Map<String, Object> getReportProperties() {
        return emptyMap();
    }

    protected Map<String, Collection<RuleInterpreterPlugin>> getRuleInterpreterPlugins() {
        return pluginRepository.getAnalyzerPluginRepository().getRuleInterpreterPlugins(getRuleInterpreterProperties());
    }

    protected Map<String, Object> getRuleInterpreterProperties() {
        return emptyMap();
    }

    /**
     * Initializes and resets the store.
     */
    private void startStore(TestInfo testInfo) throws URISyntaxException {
        Method method = testInfo.getTestMethod()
                .orElseThrow(() -> new AssertionError("Unabled to get the test method for test '" + testInfo.getDisplayName() + "'."));

        TestStore testStore = method.getAnnotation(TestStore.class);
        TestStore.Type type = testStore != null ? testStore.type() : TestStore.Type.MEMORY;
        StoreConfiguration.StoreConfigurationBuilder storeConfigurationBuilder = StoreConfiguration.builder();
        switch (type) {
        case FILE:
            String fileName = "target/jqassistant/test-store";
            storeConfigurationBuilder.uri(new File(fileName).toURI());
            storeConfigurationBuilder.embedded(getEmbeddedNeo4jConfiguration());
            break;
        case MEMORY:
            storeConfigurationBuilder.uri(new URI("memory:///"));
            storeConfigurationBuilder.embedded(getEmbeddedNeo4jConfiguration());
            break;
        case REMOTE:
            storeConfigurationBuilder.uri(new URI("bolt://localhost:7687"));
            storeConfigurationBuilder.encryption("NONE");
            storeConfigurationBuilder.username("neo4j").password("jqassistant");
            Properties properties = new Properties();
            properties.put("neo4j.remote.statement.log.level", "info");
            storeConfigurationBuilder.properties(properties);
            break;
        default:
            throw new AssertionError("Test store type not supported: " + type);
        }
        /*
         * You might break IT of depending jQAssistant plugins if you change the
         * location of the used database. Oliver B. Fischer, 2017-06-10
         */
        StoreConfiguration configuration = storeConfigurationBuilder.build();
        store = StoreFactory.getStore(configuration, pluginRepository.getStorePluginRepository());
        store.start();
        if (testStore == null || testStore.reset()) {
            store.reset();
        }
    }

    /**
     * Provide an {@link EmbeddedNeo4jConfiguration} for the file or memory store.
     *
     * @return The {@link EmbeddedNeo4jConfiguration}.
     */
    protected EmbeddedNeo4jConfiguration getEmbeddedNeo4jConfiguration() {
        return EmbeddedNeo4jConfiguration.builder().build();
    }

    /**
     * Return an initialized scanner instance.
     *
     * @return The artifact scanner instance.
     */
    protected Scanner getScanner() {
        return getScanner(getScannerProperties());
    }

    /**
     * Return an initialized scanner instance.
     *
     * @param properties
     *            The properties to be used to configure the plugins.
     * @return The artifact scanner instance.
     */
    protected Scanner getScanner(Map<String, Object> properties) {
        ScannerContext scannerContext = new ScannerContextImpl(store, outputDirectory);
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        return new ScannerImpl(getScannerConfiguration(), properties, scannerContext, scannerPluginRepository);
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
        assertThat(directory.isDirectory()).describedAs("Expected %s to be a directory", directory.toString()).isTrue();
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
        store.executeQuery("MATCH (t:Type)-[r]-() WHERE t.fqn=$className DELETE r", parameters).close();
        store.executeQuery("MATCH (t:Type) WHERE t.fqn=$className DELETE t", parameters).close();
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
        return query(query, emptyMap());
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
    protected Result<Concept> applyConcept(String id) throws RuleException {
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
    protected Result<Concept> applyConcept(String id, Map<String, String> parameters) throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder().conceptId(id).build();
        Concept concept = ruleSet.getConceptBucket().getById(id);
        assertThat(concept).describedAs("The requested concept cannot be found: " + id).isNotNull();
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
    protected Result<Constraint> validateConstraint(String id) throws RuleException {
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
    protected Result<Constraint> validateConstraint(String id, Map<String, String> parameters) throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder().constraintId(id).build();
        Constraint constraint = ruleSet.getConstraintBucket().getById(id);
        assertThat(constraint).describedAs("The requested constraint cannot be found: " + id).isNotNull();
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
        RuleSelection ruleSelection = RuleSelection.builder().groupId(id).build();
        Group group = ruleSet.getGroupsBucket().getById(id);
        assertThat(group).describedAs("The request group cannot be found: " + id).isNotNull();
        analyzer.execute(ruleSet, ruleSelection, parameters);
    }

    /**
     * @deprecated Override {@link #getReportProperties()} to configure plugins.
     */
    @Deprecated
    @ToBeRemovedInVersion(major = 1, minor = 11)
    protected Map<String, ReportPlugin> getReportPlugins(Map<String, Object> properties) {
        return getReportPlugins(reportContext, properties);
    }

    /**
     * @deprecated Override {@link #getReportProperties()} to configure plugins.
     */
    @Deprecated
    @ToBeRemovedInVersion(major = 1, minor = 11)
    protected Map<String, ReportPlugin> getReportPlugins(ReportContext reportContext, Map<String, Object> properties) {
        return pluginRepository.getAnalyzerPluginRepository().getReportPlugins(reportContext, properties);
    }

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
     * Represents a test result which allows fetching values by row or columns.
     */
    @Getter
    @AllArgsConstructor(access = PRIVATE)
    @ToString
    protected class TestResult {

        private List<Map<String, Object>> rows;
        private Map<String, List<Object>> columns;

        /**
         * Return a column identified by its name.
         *
         * @param <T>
         *            The expected type.
         * @return All columns.
         */
        public <T> List<T> getColumn(String name) {
            List<T> column = (List<T>) columns.get(name);
            if (column == null) {
                throw new IllegalArgumentException("The result does not contain a column '" + name + "'.");
            }
            return column;
        }
    }

}
