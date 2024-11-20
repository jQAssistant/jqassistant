package com.buschmais.jqassistant.core.test.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineManager;
import com.buschmais.jqassistant.core.analysis.api.baseline.BaselineRepository;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.api.configuration.Baseline;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.report.api.ReportContext;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.impl.CompositeReportPlugin;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportPlugin;
import com.buschmais.jqassistant.core.report.impl.ReportContextImpl;
import com.buschmais.jqassistant.core.resolver.api.ArtifactProviderFactory;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.rule.api.reader.RuleParserPlugin;
import com.buschmais.jqassistant.core.rule.api.source.FileRuleSource;
import com.buschmais.jqassistant.core.rule.api.source.RuleSource;
import com.buschmais.jqassistant.core.rule.impl.reader.RuleParser;
import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.scanner.impl.ScannerContextImpl;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.scanner.spi.ScannerPluginRepository;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationBuilder;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.StoreFactory;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import io.smallrye.config.EnvConfigSource;
import io.smallrye.config.SysPropConfigSource;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static lombok.AccessLevel.PRIVATE;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Abstract base class for analysis tests.
 */
public abstract class AbstractPluginIT {

    private static final File USER_HOME = new File(System.getProperty("user.home"));

    private static final File WORKING_DIRECTORY = new File(".");

    private static final File OUTPUT_DIRECTORY = new File(WORKING_DIRECTORY, "target/jqassistant");

    private static final File TEST_STORE_DIRECTORY = new File("target/jqassistant/test-store");

    protected static final String ARTIFACT_ID = "artifact";

    private static ArtifactProviderFactory artifactProviderFactory;

    private static PluginRepositoryImpl pluginRepository;

    protected Store store;

    protected InMemoryReportPlugin reportPlugin;

    protected RuleSet ruleSet;

    @BeforeAll
    public static final void initPluginRepository() {
        artifactProviderFactory = new ArtifactProviderFactory(USER_HOME);
        OUTPUT_DIRECTORY.mkdirs();
        PluginClassLoader pluginClassLoader = new PluginClassLoader(AbstractPluginIT.class.getClassLoader());
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        pluginRepository.initialize();
    }

    @AfterAll
    public static final void destroyPluginRepository() {
        if (pluginRepository != null) {
            pluginRepository.destroy();
        }
    }

    @BeforeEach
    public void beforeEach() throws IOException, RuleException {
        ConfigurationBuilder configurationBuilder = createConfigurationBuilder();
        configure(configurationBuilder);
        ITConfiguration configuration = createConfiguration(configurationBuilder);
        startStore(configuration);
        initializeRuleSet(configuration);
        initializeReportPlugin(configuration);
    }

    protected void configure(ConfigurationBuilder configurationBuilder) {
    }

    protected List<String> getConfigurationProfiles() {
        return emptyList();
    }

    private ConfigurationBuilder createConfigurationBuilder() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder("ITConfigSource", 110);
        configurationBuilder.with(Report.class, Report.PROPERTIES, getReportProperties());
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
    private ITConfiguration createConfiguration(ConfigurationBuilder configurationBuilder) {
        return ConfigurationMappingLoader.builder(ITConfiguration.class)
            .withClasspath()
            .withProfiles(getConfigurationProfiles())
            .load(configurationBuilder.build(), new EnvConfigSource() {
            }, new SysPropConfigSource());
    }

    private void initializeRuleSet(Configuration configuration) throws RuleException, IOException {
        File rulesDirectory = getRulesDirectory();
        // read rules from rules directory
        List<RuleSource> sources = new LinkedList<>();
        if (rulesDirectory.exists()) {
            sources.addAll(FileRuleSource.getRuleSources(rulesDirectory));
        }
        // read rules from plugins
        sources.addAll(pluginRepository.getRulePluginRepository()
            .getRuleSources());
        Collection<RuleParserPlugin> ruleParserPlugins = pluginRepository.getRulePluginRepository()
            .getRuleParserPlugins(configuration.analyze()
                .rule());
        RuleParser ruleParser = new RuleParser(ruleParserPlugins);
        ruleSet = ruleParser.parse(sources);
    }

    private File getRulesDirectory() {
        return new File(getClassesDirectory(this.getClass()), "rules");
    }

    private void initializeReportPlugin(Configuration configuration) {
        ReportContext reportContext = new ReportContextImpl(pluginRepository.getClassLoader(), store, OUTPUT_DIRECTORY);
        Map<String, ReportPlugin> reportPlugins = pluginRepository.getAnalyzerPluginRepository()
            .getReportPlugins(configuration.analyze()
                .report(), reportContext);
        this.reportPlugin = new InMemoryReportPlugin(new CompositeReportPlugin(reportPlugins));
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
        return pluginRepository.getAnalyzerPluginRepository()
            .getRuleInterpreterPlugins(getRuleInterpreterProperties());
    }

    protected Map<String, Object> getRuleInterpreterProperties() {
        return emptyMap();
    }

    /**
     * Initializes and resets the store.
     */
    private void startStore(ITConfiguration configuration) {
        ArtifactProvider artifactProvider = artifactProviderFactory.create(configuration);
        StoreFactory storeFactory = new StoreFactory(pluginRepository.getStorePluginRepository(), artifactProvider);
        store = storeFactory.getStore(configuration.store(), () -> TEST_STORE_DIRECTORY);
        store.start();
        store.reset();
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
        ScannerContext scannerContext = new ScannerContextImpl(pluginRepository.getClassLoader(), store, WORKING_DIRECTORY, OUTPUT_DIRECTORY);
        ScannerPluginRepository scannerPluginRepository = pluginRepository.getScannerPluginRepository();
        return new ScannerImpl(configuration.scan(), scannerContext, scannerPluginRepository);
    }

    private Analyzer getAnalyzer(Map<String, String> parameters) {
        ConfigurationBuilder configurationBuilder = createConfigurationBuilder().with(Analyze.class, Analyze.RULE_PARAMETERS, parameters);
        Configuration configuration = createConfiguration(configurationBuilder);
        Baseline baselineConfiguration = configuration.analyze()
            .baseline();
        BaselineRepository baselineRepository = new BaselineRepository(baselineConfiguration, getRulesDirectory());
        BaselineManager baselineManager = new BaselineManager(baselineConfiguration, baselineRepository);
        return new AnalyzerImpl(configuration.analyze(), pluginRepository.getClassLoader(), store, getRuleInterpreterPlugins(), baselineManager, reportPlugin);
    }

    /**
     * Determines the directory a class is located in (e.g. target/test-classes).
     *
     * @param rootClass
     *     The class.
     * @return The directory.
     */
    protected File getClassesDirectory(Class<?> rootClass) {
        String path = URLDecoder.decode(rootClass.getClassLoader()
            .getResource(".")
            .getPath(), Charset.defaultCharset());
        File directory = new File(path);
        assertThat(directory).isDirectory()
            .describedAs("Expected %s to be a directory", directory.toString());
        return directory;
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractPluginIT.TestResult} .
     *
     * @param query
     *     The query.
     * @return The {@link AbstractPluginIT.TestResult}.
     */
    protected TestResult query(String query) {
        return query(query, emptyMap());
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractPluginIT.TestResult} .
     *
     * @param query
     *     The query.
     * @param parameters
     *     The query parameters.
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
     *     The id.
     * @return The result.
     */
    protected Result<Concept> applyConcept(String id) throws RuleException {
        return applyConcept(id, emptyMap());
    }

    /**
     * Applies the concept identified by id.
     *
     * @param id
     *     The id.
     * @param parameters
     *     The rule parameters.
     * @return The result.
     */
    protected Result<Concept> applyConcept(String id, Map<String, String> parameters) throws RuleException {
        Analyzer analyzer = getAnalyzer(parameters);
        RuleSelection ruleSelection = RuleSelection.builder()
            .conceptId(id)
            .build();
        Concept concept = ruleSet.getConceptBucket()
            .getById(id);
        assertThat(concept).describedAs("The requested concept cannot be found: " + id)
            .isNotNull();
        analyzer.execute(ruleSet, ruleSelection);
        return reportPlugin.getConceptResults()
            .get(id);
    }

    /**
     * Validates the constraint identified by id.
     *
     * @param id
     *     The id.
     * @return The result.
     */
    protected Result<Constraint> validateConstraint(String id) throws RuleException {
        return validateConstraint(id, Collections.<String, String>emptyMap());
    }

    /**
     * Validates the constraint identified by id.
     *
     * @param id
     *     The id.
     * @param parameters
     *     The rule parameters.
     * @return The result.
     */
    protected Result<Constraint> validateConstraint(String id, Map<String, String> parameters) throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder()
            .constraintId(id)
            .build();
        Constraint constraint = ruleSet.getConstraintBucket()
            .getById(id);
        assertThat(constraint).describedAs("The requested constraint cannot be found: " + id)
            .isNotNull();
        getAnalyzer(parameters).execute(ruleSet, ruleSelection);
        return reportPlugin.getConstraintResults()
            .get(id);
    }

    /**
     * Executes the group identified by id.
     *
     * @param id
     *     The id.
     */
    protected void executeGroup(String id) throws RuleException {
        executeGroup(id, Collections.<String, String>emptyMap());
    }

    /**
     * Executes the group identified by id.
     *
     * @param id
     *     The id.
     * @param parameters
     *     The rule parameters.
     */
    protected void executeGroup(String id, Map<String, String> parameters) throws RuleException {
        RuleSelection ruleSelection = RuleSelection.builder()
            .groupId(id)
            .build();
        Group group = ruleSet.getGroupsBucket()
            .getById(id);
        assertThat(group).describedAs("The request group cannot be found: " + id)
            .isNotNull();
        getAnalyzer(parameters).execute(ruleSet, ruleSelection);
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
         *     The expected type.
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
