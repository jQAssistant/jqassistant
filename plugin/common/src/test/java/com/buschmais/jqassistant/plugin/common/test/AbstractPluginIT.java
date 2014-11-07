package com.buschmais.jqassistant.plugin.common.test;

import static com.buschmais.xo.api.Query.Result;
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
import com.buschmais.jqassistant.core.plugin.impl.ModelPluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.plugin.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.scanner.api.Scanner;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDirectoryDescriptor;
import com.buschmais.jqassistant.plugin.common.test.matcher.TestConsole;

/**
 * Abstract base class for analysis tests.
 */
public class AbstractPluginIT {

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

    private PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(AbstractPluginIT.class.getClassLoader());
    private RulePluginRepository rulePluginRepository;
    private ModelPluginRepository modelPluginRepository;
    private ScannerPluginRepository scannerPluginRepository;

    @Before
    public void readRules() throws PluginRepositoryException {
        rulePluginRepository = new RulePluginRepositoryImpl(pluginConfigurationReader);
        List<RuleSource> sources = rulePluginRepository.getRuleSources();
        RuleSetReader ruleSetReader = new CompoundRuleSetReader();
        ruleSet = ruleSetReader.read(sources);
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
        store = new EmbeddedGraphStore("target/jqassistant/" + this.getClass().getSimpleName());
        modelPluginRepository = new ModelPluginRepositoryImpl(pluginConfigurationReader);
        scannerPluginRepository = new ScannerPluginRepositoryImpl(pluginConfigurationReader, Collections.<String, Object> emptyMap());
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
        return new ScannerImpl(store, getScannerPlugins());
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
        Result<CompositeRowObject> compositeRowObjects = store.executeQuery(query, parameters);
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
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the analyzer reports an error.
     */
    protected void applyConcept(String id) throws AnalysisException {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConceptId(id).get();
        Concept concept = ruleSet.getConcepts().get(id);
        assertNotNull("The requested concept cannot be resolved.", concept);
        analyzer.execute(ruleSet, ruleSelection);
    }

    /**
     * Validates the constraint identified by id.
     * 
     * @param id
     *            The id.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the analyzer reports an error.
     */
    protected void validateConstraint(String id) throws AnalysisException {
        RuleSelection ruleSelection = RuleSelection.Builder.newInstance().addConstraintId(id).get();
        Constraint constraint = ruleSet.getConstraints().get(id);
        assertNotNull("The constraint must not be null", constraint);
        analyzer.execute(ruleSet, ruleSelection);
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
        assertNotNull("The group must not be null", group);
        analyzer.execute(ruleSet, ruleSelection);
    }

    /**
     * Get or create an
     * {@link com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor}
     * .
     * 
     * @param artifactId
     *            The artifact id.
     * @return The
     *         {@link com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor}
     *         .
     */
    protected ArtifactDirectoryDescriptor getArtifactDescriptor(String artifactId) {
        ArtifactDescriptor artifact = store.find(ArtifactDescriptor.class, artifactId);
        if (artifact == null) {
            artifact = store.create(ArtifactDirectoryDescriptor.class, artifactId);
        }
        return ArtifactDirectoryDescriptor.class.cast(artifact);
    }

    private List<Class<?>> getDescriptorTypes() {
        try {
            return modelPluginRepository.getDescriptorTypes();
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get descriptor mappers.", e);
        }
    }

    private List<ScannerPlugin<?, ?>> getScannerPlugins() {
        try {
            return scannerPluginRepository.getScannerPlugins();
        } catch (PluginRepositoryException e) {
            throw new IllegalStateException("Cannot get scanner plugins.", e);
        }
    }

    protected ScannerPluginRepository getScannerPluginRepository() {
        return scannerPluginRepository;
    }

    protected RulePluginRepository getRulePluginRepository() {
        return rulePluginRepository;
    }
}
