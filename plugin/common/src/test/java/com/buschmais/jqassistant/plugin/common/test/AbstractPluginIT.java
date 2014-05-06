package com.buschmais.jqassistant.plugin.common.test;

import static com.buschmais.xo.api.Query.Result;
import static com.buschmais.xo.api.Query.Result.CompositeRowObject;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

import javax.xml.transform.Source;

import com.buschmais.jqassistant.plugin.common.test.matcher.TestConsole;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerException;
import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.analysis.api.RuleSetReader;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleSet;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerImpl;
import com.buschmais.jqassistant.core.analysis.impl.RuleSetReaderImpl;
import com.buschmais.jqassistant.core.pluginrepository.api.RulePluginRepository;
import com.buschmais.jqassistant.core.pluginrepository.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.pluginrepository.impl.RulePluginRepositoryImpl;
import com.buschmais.jqassistant.core.pluginrepository.impl.ScannerPluginRepositoryImpl;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.FileScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor;

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

    private RulePluginRepository rulePluginRepository;
    private ScannerPluginRepository scannerPluginRepository;

    @Before
    public void readRules() throws PluginReaderException {
        rulePluginRepository = new RulePluginRepositoryImpl();
        List<Source> sources = rulePluginRepository.getRuleSources();
        RuleSetReader ruleSetReader = new RuleSetReaderImpl();
        ruleSet = ruleSetReader.read(sources);
        Assert.assertTrue("There must be no unresolved concepts.", ruleSet.getMissingConcepts().isEmpty());
        Assert.assertTrue("There must be no unresolved result.", ruleSet.getMissingConstraints().isEmpty());
        Assert.assertTrue("There must be no unresolved groups.", ruleSet.getMissingGroups().isEmpty());
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
    public void startStore() throws PluginReaderException {
        store = new EmbeddedGraphStore("target/jqassistant/" + this.getClass().getSimpleName());
        scannerPluginRepository = new ScannerPluginRepositoryImpl(store, new Properties());
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
     * Return an initialized artifact scanner instance.
     * 
     * @return The artifact scanner instance.
     */
    protected FileScanner getFileScanner() {
        return new FileScannerImpl(getFileScannerPlugins());
    }

    /**
     * Scans the given classes.
     * 
     * @param classes
     *            The classes.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    protected void scanClasses(Class<?>... classes) throws IOException {
        this.scanClasses(ARTIFACT_ID, classes);
    }

    /**
     * Scans the given classes.
     * 
     * @param outerClass
     *            The outer classes.
     * @param innerClassName
     *            The outer classes.
     * @throws java.io.IOException
     *             If scanning fails.
     */
    protected void scanInnerClass(Class<?> outerClass, String innerClassName) throws IOException, ClassNotFoundException {
        Class<?> innerClass = getInnerClass(outerClass, innerClassName);
        scanClasses(innerClass);
    }

    /**
     * Loads an inner class.
     * 
     * @param outerClass
     *            The out class.
     * @param innerClassName
     *            The name of the inner class.
     * @return The inner class.
     * @throws ClassNotFoundException
     *             If the class cannot be loaded.
     */
    protected Class<?> getInnerClass(Class<?> outerClass, String innerClassName) throws ClassNotFoundException {
        String className = outerClass.getName() + "$" + innerClassName;
        return outerClass.getClassLoader().loadClass(className);
    }

    /**
     * Scans the given classes.
     * 
     * @param artifactId
     *            The id of the containing artifact.
     * @param classes
     *            The classes.
     * @throws IOException
     *             If scanning fails.
     */
    protected void scanClasses(String artifactId, Class<?>... classes) throws IOException {
        store.beginTransaction();
        ArtifactDescriptor artifact = getArtifactDescriptor(artifactId);
        for (FileDescriptor descriptor : getFileScanner().scanClasses(classes)) {
            artifact.addContains(descriptor);
        }
        store.commitTransaction();
    }

    /**
     * Scans the given URLs.
     * 
     * @param urls
     *            The URLs.
     * @throws IOException
     *             If scanning fails.
     */
    protected void scanURLs(URL... urls) throws IOException {
        this.scanURLs(ARTIFACT_ID, urls);
    }

    /**
     * Scans the given URLs (e.g. for anonymous inner classes).
     * 
     * @param artifactId
     *            The id of the containing artifact.
     * @param urls
     *            The URLs.
     * @throws IOException
     *             If scanning fails.
     */
    protected void scanURLs(String artifactId, URL... urls) throws IOException {
        store.beginTransaction();
        ArtifactDescriptor artifact = artifactId != null ? getArtifactDescriptor(artifactId) : null;
        for (FileDescriptor descriptor : getFileScanner().scanURLs(urls)) {
            artifact.addContains(descriptor);
        }
        store.commitTransaction();
    }

    /**
     * Scans the test classes directory.
     * 
     * @param rootClass
     *            A class within the test directory.
     * @throws IOException
     *             If scanning fails.
     */
    protected void scanClassesDirectory(Class<?> rootClass) throws IOException {
        File directory = getClassesDirectory(rootClass);
        scanDirectory(directory);
    }

    /**
     * Scans the a directory.
     * 
     * @param directory
     *            The directory.
     * @throws IOException
     *             If scanning fails.
     */
    protected void scanDirectory(File directory) throws IOException {
        // Scan.
        store.beginTransaction();
        ArtifactDescriptor artifact = getArtifactDescriptor(ARTIFACT_ID);
        for (FileDescriptor descriptor : getFileScanner().scanDirectory(directory)) {
            artifact.addContains(descriptor);
        }
        store.commitTransaction();
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
        Assert.assertTrue("Expected a directory.", directory.isDirectory());
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
        store.executeQuery("MATCH (t:TYPE)-[r]-() WHERE t.FQN={className} DELETE r", parameters).close();
        store.executeQuery("MATCH (t:TYPE) WHERE t.FQN={className} DELETE t", parameters).close();
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
     * @throws AnalyzerException
     *             If the analyzer reports an error.
     */
    protected void applyConcept(String id) throws AnalyzerException {
        Concept concept = ruleSet.getConcepts().get(id);
        Assert.assertNotNull("The concept must not be null", concept);
        RuleSet targetRuleSet = new RuleSet();
        targetRuleSet.getConcepts().put(concept.getId(), concept);
        analyzer.execute(targetRuleSet);
    }

    /**
     * Validates the constraint identified by id.
     * 
     * @param id
     *            The id.
     * @throws AnalyzerException
     *             If the analyzer reports an error.
     */
    protected void validateConstraint(String id) throws AnalyzerException {
        Constraint constraint = ruleSet.getConstraints().get(id);
        Assert.assertNotNull("The constraint must not be null", constraint);
        RuleSet targetRuleSet = new RuleSet();
        targetRuleSet.getConstraints().put(constraint.getId(), constraint);
        analyzer.execute(targetRuleSet);
    }

    /**
     * Executes the group identified by id.
     * 
     * @param id
     *            The id.
     * @throws AnalyzerException
     *             If the analyzer reports an error.
     */
    protected void executeGroup(String id) throws AnalyzerException {
        Group group = ruleSet.getGroups().get(id);
        Assert.assertNotNull("The group must not be null", group);
        RuleSet targetRuleSet = new RuleSet();
        targetRuleSet.getGroups().put(group.getId(), group);
        analyzer.execute(targetRuleSet);
    }

    /**
     * Get or create an
     * {@link com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor}
     * .
     * 
     * @param artifactId
     *            The artifact id.
     * @return The
     *         {@link com.buschmais.jqassistant.plugin.common.impl.store.descriptor.ArtifactDescriptor}
     *         .
     */
    private ArtifactDescriptor getArtifactDescriptor(String artifactId) {
        ArtifactDescriptor artifact = store.find(ArtifactDescriptor.class, artifactId);
        if (artifact == null) {
            artifact = store.create(ArtifactDescriptor.class, artifactId);
        }
        return artifact;
    }

    private List<Class<?>> getDescriptorTypes() {
        try {
            return scannerPluginRepository.getDescriptorTypes();
        } catch (PluginReaderException e) {
            throw new IllegalStateException("Cannot get descriptor mappers.", e);
        }
    }

    private List<FileScannerPlugin> getFileScannerPlugins() {
        try {
            return scannerPluginRepository.getFileScannerPlugins();
        } catch (PluginReaderException e) {
            throw new IllegalStateException("Cannot get scanner plugins.", e);
        }
    }
}
