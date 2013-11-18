package com.buschmais.jqassistant.plugin.common.test;

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
import com.buschmais.jqassistant.core.pluginmanager.api.PluginManager;
import com.buschmais.jqassistant.core.pluginmanager.impl.PluginManagerImpl;
import com.buschmais.jqassistant.core.report.impl.InMemoryReportWriter;
import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.FileScannerImpl;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.ArtifactDescriptor;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;

import javax.xml.transform.Source;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static com.buschmais.cdo.api.Query.Result;
import static com.buschmais.cdo.api.Query.Result.CompositeRowObject;

/**
 * Abstract base class for analysis tests.
 */
public class AbstractPluginIT {

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

	private PluginManager pluginManager = new PluginManagerImpl();

	@BeforeClass
	public static void readRules() {
		PluginManager pluginManager = new PluginManagerImpl();
		List<Source> sources = pluginManager.getRuleSources();
		RuleSetReader ruleSetReader = new RuleSetReaderImpl();
		ruleSet = ruleSetReader.read(sources);
		Assert.assertTrue("There must be no unresolved concepts.", ruleSet.getMissingConcepts().isEmpty());
		Assert.assertTrue("There must be no unresolved result.", ruleSet.getMissingConstraints().isEmpty());
		Assert.assertTrue("There must be no unresolved groups.", ruleSet.getMissingGroups().isEmpty());
	}

	@Before
	public void initializeAnalyzer() {
		reportWriter = new InMemoryReportWriter();
		analyzer = new AnalyzerImpl(store, reportWriter);
	}

	/**
	 * The store.
	 */
	protected Store store;

	/**
	 * Initializes and resets the store.
	 */
	@Before
	public void startStore() {
		store = new EmbeddedGraphStore("target/jqassistant/" + this.getClass().getSimpleName());
		store.start(getDescriptorMappers());
		store.reset();
        store.beginTransaction();
	}

	/**
	 * Stops the store.
	 */
	@After
	public void stopStore() {
        store.commitTransaction();
		store.stop();
	}

	/**
	 * Return an initialized artifact scanner instance.
	 *
	 * @return The artifact scanner instance.
	 */
	protected FileScanner getArtifactScanner() {
		return new FileScannerImpl(store, getScannerPlugins());
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
		this.scanClasses("test", classes);
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
		ArtifactDescriptor artifact = store.find(ArtifactDescriptor.class, artifactId);
		if (artifact == null) {
			artifact = store.create(ArtifactDescriptor.class, artifactId);
		}
		for (Descriptor descriptor : getArtifactScanner().scanClasses(classes)) {
			artifact.getContains().add(descriptor);
		}
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
		this.scanURLs("test", urls);
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
		ArtifactDescriptor artifact = artifactId != null ? store.create(ArtifactDescriptor.class, artifactId) : null;
		for (Descriptor descriptor : getArtifactScanner().scanURLs(urls)) {
			artifact.getContains().add(descriptor);
		}
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
		// Determine test classes directory.
		URL resource = rootClass.getResource("/");
		String file = resource.getFile();
		File directory = new File(file);
		Assert.assertTrue("Expected a directory.", directory.isDirectory());
		// Scan.
		store.beginTransaction();
		ArtifactDescriptor artifact = store.create(ArtifactDescriptor.class, "artifact");
		for (Descriptor descriptor : getArtifactScanner().scanDirectory(directory)) {
			artifact.getContains().add(descriptor);
		}
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
        List<String> columnNames = compositeRowObjects.getColumns();
        for (String column : columnNames) {
			columns.put(column, new ArrayList<>());
		}
		for (CompositeRowObject rowObject : compositeRowObjects) {
			Map<String, Object> row = new HashMap<>();
            for (String columnName : columnNames) {
                Object value = rowObject.get(columnName, Object.class);
                row.put(columnName, value);
                columns.get(columnName).add(value);
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

	private List<Class<?>> getDescriptorMappers() {
		try {
			return pluginManager.getDescriptorTypes();
		} catch (PluginReaderException e) {
			throw new IllegalStateException("Cannot get descriptor mappers.", e);
		}
	}

	private List<FileScannerPlugin<?>> getScannerPlugins() {
		try {
			return pluginManager.getScannerPlugins();
		} catch (PluginReaderException e) {
			throw new IllegalStateException("Cannot get scanner plugins.", e);
		}
	}
}
