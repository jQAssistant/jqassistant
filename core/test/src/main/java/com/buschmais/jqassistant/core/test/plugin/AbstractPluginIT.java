package com.buschmais.jqassistant.core.test.plugin;

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
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationBuilder;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.configuration.impl.ConfigurationLoaderImpl;
import com.buschmais.jqassistant.core.plugin.api.PluginConfigurationReader;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.shared.io.ClasspathResource;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
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

    public static final File TEST_STORE_DIRECTORY = new File("target/jqassistant/test-store");

    protected static final String ARTIFACT_ID = "artifact";

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractPluginIT.class);


    private static PluginRepositoryImpl pluginRepository;

    private File outputDirectory;

    private TestStore testStore;

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
        Method method = testInfo.getTestMethod()
            .orElseThrow(() -> new AssertionError("Unable to get the test method for test '" + testInfo.getDisplayName() + "'."));
        testStore = method.getAnnotation(TestStore.class);
        Configuration configuration = createConfiguration(createConfigurationBuilder());
        outputDirectory = new File("target/jqassistant");
        outputDirectory.mkdirs();
        startStore(configuration.store(), testStore);
        initializeRuleSet(configuration);
        initializeAnalyzer(configuration);
    }

    protected ConfigurationBuilder createConfigurationBuilder() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("ITConfigSource", 110);
        TestStore.Type type = testStore != null ? testStore.type() : TestStore.Type.FILE;
        switch (type) {
        case FILE:
            break;
        case MEMORY:
            try {
                configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                    com.buschmais.jqassistant.core.store.api.configuration.Store.URI, new URI("memory:///"));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot create store URI", e);
            }
            break;
        case REMOTE:
            try {
                configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                    com.buschmais.jqassistant.core.store.api.configuration.Store.URI, new URI("bolt://localhost:7687"));
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Cannot create store URI", e);
            }
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.ENCRYPTION, "NONE");
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.USERNAME, "neo4j");
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.PASSWORD, "jqassistant");
            Properties properties = new Properties();
            properties.put("neo4j.remote.statement.log.level", "info");
            configurationBuilder.with(com.buschmais.jqassistant.core.store.api.configuration.Store.class,
                com.buschmais.jqassistant.core.store.api.configuration.Store.PROPERTIES, properties);
            break;
        default:
            throw new AssertionError("Test store type not supported: " + type);
        }
        return configurationBuilder;
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

    /**
     * Load configuration for ITs.
     *
     * @return The  configuration.
     */
    private Configuration createConfiguration(ConfigurationBuilder configurationBuilder) {
        ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl();
        return configurationLoader.load(Configuration.class, configurationBuilder.build());
    }

    private void initializeRuleSet(Configuration configuration) throws RuleException, IOException {
        File selectedDirectory = new File(getClassesDirectory(this.getClass()), "rules");
        // read rules from rules directory
        List<RuleSource> sources = new LinkedList<>();
        if (selectedDirectory.exists()) {
            sources.addAll(FileRuleSource.getRuleSources(selectedDirectory));
        }
        // read rules from plugins
        sources.addAll(pluginRepository.getRulePluginRepository().getRuleSources());
        Collection<RuleParserPlugin> ruleParserPlugins = pluginRepository.getRulePluginRepository()
            .getRuleParserPlugins(configuration.analyze()
                .rule());
        RuleParser ruleParser = new RuleParser(ruleParserPlugins);
        ruleSet = ruleParser.parse(sources);
    }

    private void initializeAnalyzer(Configuration configuration) {
        this.reportContext = new ReportContextImpl(store, outputDirectory);
        this.reportPlugin = getReportPlugin();
        this.analyzer = getAnalyzer(configuration);
    }

    /**
     * Return the properties for the scanner, to be overwritten by sub-classes.
     */
    protected Map<String, Object> getScannerProperties() {
        return emptyMap();
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
    private void startStore(com.buschmais.jqassistant.core.store.api.configuration.Store storeConfiguration, TestStore testStore) {
        store = StoreFactory.getStore(storeConfiguration, () -> TEST_STORE_DIRECTORY, pluginRepository.getStorePluginRepository());
        store.start();
        if (testStore == null || testStore.reset()) {
            store.reset();
        }
    }

    /**
     * Return an initialized scanner instance using the default configuration.
     *
     * @return The scanner instance.
     */
    protected Scanner getScanner() {
        return getScanner(getScannerProperties());
    }

    /**
     * Return an initialized scanner instance using the given properties.
     *
     * @return The scanner instance.
     */
    protected Scanner getScanner(Map<String, Object> properties) {
        ConfigurationBuilder configurationBuilder = createConfigurationBuilder().with(Scan.class, Scan.PROPERTIES, properties);
        Configuration configuration = createConfiguration(configurationBuilder);
        return getScanner(configuration);
    }

    private Scanner getScanner(Configuration configuration) {
        ScannerContext scannerContext = new ScannerContextImpl(store, outputDirectory);
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        return new ScannerImpl(configuration.scan(), scannerContext, scannerPluginRepository);
    }

    private Analyzer getAnalyzer(Map<String, String> parameters) {
        ConfigurationBuilder configurationBuilder = createConfigurationBuilder().with(Analyze.class, Analyze.RULE_PARAMETERS, parameters);
        Configuration configuration = createConfiguration(configurationBuilder);
        return getAnalyzer(configuration);
    }

    private Analyzer getAnalyzer(Configuration configuration) {
        return new AnalyzerImpl(configuration.analyze(), store, getRuleInterpreterPlugins(), reportPlugin, LOGGER);
    }

    private InMemoryReportPlugin getReportPlugin() {
        ConfigurationBuilder configurationBuilder = createConfigurationBuilder().with(Report.class, Report.PROPERTIES, getReportProperties());
        Configuration configuration = createConfiguration(configurationBuilder);
        return getReportPlugin(configuration);
    }

    private InMemoryReportPlugin getReportPlugin(Configuration configuration) {
        Map<String, ReportPlugin> reportPlugins = pluginRepository.getAnalyzerPluginRepository()
            .getReportPlugins(configuration.analyze()
                .report(), reportContext);
        return new InMemoryReportPlugin(new CompositeReportPlugin(reportPlugins));
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
        Analyzer analyzer = getAnalyzer(parameters);
        RuleSelection ruleSelection = RuleSelection.builder().conceptId(id).build();
        Concept concept = ruleSet.getConceptBucket().getById(id);
        assertThat(concept).describedAs("The requested concept cannot be found: " + id).isNotNull();
        analyzer.execute(ruleSet, ruleSelection);
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
        getAnalyzer(parameters).execute(ruleSet, ruleSelection);
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
        getAnalyzer(parameters).execute(ruleSet, ruleSelection);
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
