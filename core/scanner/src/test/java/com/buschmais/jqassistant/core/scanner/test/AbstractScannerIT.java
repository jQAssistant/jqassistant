package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.scanner.api.ClassScanner;
import com.buschmais.jqassistant.core.scanner.impl.ClassScannerImpl;
import com.buschmais.jqassistant.core.store.api.QueryResult;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Abstract base class for test using the class scanner.
 */
public abstract class AbstractScannerIT {

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
        store.start();
        store.beginTransaction();
        store.reset();
        store.commitTransaction();
    }

    /**
     * Stops the store.
     */
    @After
    public void stopStore() {
        store.stop();
    }

    /**
     * Return an initialized class scanner instance.
     *
     * @return The class scanner instance.
     */
    protected ClassScanner getScanner() {
        return new ClassScannerImpl(store, getScanListener());
    }

    /**
     * Return the {@link com.buschmais.jqassistant.core.scanner.impl.ClassScannerImpl.ScanListener} to be used for scanning.
     * <p>The default implementation returns a listener without any functionality, a class may override this method to return a listener implementing specific behavior.</p>
     *
     * @return The {@link com.buschmais.jqassistant.core.scanner.impl.ClassScannerImpl.ScanListener}.
     */
    protected ClassScannerImpl.ScanListener getScanListener() {
        return new ClassScannerImpl.ScanListener() {
        };
    }


    /**
     * Scans the given classes.
     *
     * @param classes The classes.
     * @throws IOException If scanning fails.
     */
    protected void scanClasses(Class<?>... classes) throws IOException {
        this.scanClasses(null, classes);
    }

    /**
     * Scans the given classes.
     *
     * @param artifactId The id of the containing artifact.
     * @param classes    The classes.
     * @throws IOException If scanning fails.
     */
    protected void scanClasses(String artifactId, Class<?>... classes) throws IOException {
        store.beginTransaction();
        ArtifactDescriptor artifact = artifactId != null ? store.create(ArtifactDescriptor.class, artifactId) : null;
        getScanner().scanClasses(artifact, classes);
        store.commitTransaction();
    }

    /**
     * Scans the classes given as resource names (e.g. for anonymous inner classes).
     *
     * @param resourceNames The classes.
     * @throws IOException If scanning fails.
     */
    protected void scanClassResources(String... resourceNames) throws IOException {
        store.beginTransaction();
        for (String resourceName : resourceNames) {
            InputStream is = AnonymousInnerClassIT.class.getResourceAsStream(resourceName);
            getScanner().scanInputStream(null, is, resourceName);
        }
        store.commitTransaction();
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractScannerIT.TestResult}.
     *
     * @param query The query.
     * @return The  {@link AbstractScannerIT.TestResult}.
     */
    protected TestResult executeQuery(String query) {
        return executeQuery(query, Collections.<String, Object>emptyMap());
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractScannerIT.TestResult}.
     *
     * @param query      The query.
     * @param parameters The query parameters.
     * @return The  {@link AbstractScannerIT.TestResult}.
     */
    protected TestResult executeQuery(String query, Map<String, Object> parameters) {
        store.beginTransaction();
        QueryResult queryResult = store.executeQuery(query, parameters);
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        Map<String, List<Object>> columns = new HashMap<String, List<Object>>();
        for (String column : queryResult.getColumns()) {
            columns.put(column, new ArrayList<Object>());
        }
        for (QueryResult.Row row : queryResult.getRows()) {
            Map<String, Object> rowData = row.get();
            rows.add(rowData);
            for (Map.Entry<String, ?> entry : rowData.entrySet()) {
                List<Object> column = columns.get(entry.getKey());
                column.add(entry.getValue());
            }
        }
        store.commitTransaction();
        return new TestResult(rows, columns);
    }

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
}
