package com.buschmais.jqassistant.core.scanner.test;

import com.buschmais.jqassistant.core.model.api.descriptor.ArtifactDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;
import com.buschmais.jqassistant.core.scanner.api.FileScanner;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.ClassScannerPlugin;
import com.buschmais.jqassistant.core.scanner.impl.FileScannerImpl;
import com.buschmais.jqassistant.core.scanner.impl.PackageScannerPlugin;
import com.buschmais.jqassistant.core.store.api.QueryResult;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.*;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.net.URL;
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
        store.start(getDescriptorMappers());
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
     * Return the list of descriptor mappers used by the test.
     *
     * @return The descriptor mappers.
     */
    protected List<DescriptorMapper<?>> getDescriptorMappers() {
        List<DescriptorMapper<?>> mappers = new ArrayList<>();
        mappers.add(new ArtifactDescriptorMapper());
        mappers.add(new PackageDescriptorMapper());
        mappers.add(new TypeDescriptorMapper());
        mappers.add(new MethodDescriptorMapper());
        mappers.add(new ParameterDescriptorMapper());
        mappers.add(new FieldDescriptorMapper());
        mappers.add(new ValueDescriptorMapper());
        return mappers;
    }

    /**
     * Return the list of scanner plugins used by the test.
     *
     * @return The scanner plugins.
     */
    protected List<FileScannerPlugin<?>> getScannerPlugins() {
        List<FileScannerPlugin<?>> plugins = new ArrayList<>();
        plugins.add(new PackageScannerPlugin());
        plugins.add(new ClassScannerPlugin());
        return plugins;
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
     * @param classes The classes.
     * @throws IOException If scanning fails.
     */
    protected void scanClasses(Class<?>... classes) throws IOException {
        this.scanClasses("test", classes);
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
        ArtifactDescriptor artifact = store.find(ArtifactDescriptor.class, artifactId);
        if (artifact == null) {
            artifact = store.create(ArtifactDescriptor.class, artifactId);
        }
        for (Descriptor descriptor : getArtifactScanner().scanClasses(classes)) {
            artifact.getContains().add(descriptor);
        }
        store.commitTransaction();
    }

    /**
     * Scans the given URLs.
     *
     * @param urls The URLs.
     * @throws IOException If scanning fails.
     */
    protected void scanURLs(URL... urls) throws IOException {
        this.scanURLs("test", urls);
    }

    /**
     * Scans the given URLs (e.g. for anonymous inner classes).
     *
     * @param artifactId The id of the containing artifact.
     * @param urls       The URLs.
     * @throws IOException If scanning fails.
     */
    protected void scanURLs(String artifactId, URL... urls) throws IOException {
        store.beginTransaction();
        ArtifactDescriptor artifact = artifactId != null ? store.create(ArtifactDescriptor.class, artifactId) : null;
        for (Descriptor descriptor : getArtifactScanner().scanURLs(urls)) {
            artifact.getContains().add(descriptor);
        }
        store.commitTransaction();
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractScannerIT.TestResult}.
     *
     * @param query The query.
     * @return The  {@link AbstractScannerIT.TestResult}.
     */
    protected TestResult query(String query) {
        return query(query, Collections.<String, Object>emptyMap());
    }

    /**
     * Executes a CYPHER query and returns a {@link AbstractScannerIT.TestResult}.
     *
     * @param query      The query.
     * @param parameters The query parameters.
     * @return The  {@link AbstractScannerIT.TestResult}.
     */
    protected TestResult query(String query, Map<String, Object> parameters) {
        store.beginTransaction();
        QueryResult queryResult = store.executeQuery(query, parameters);
        List<Map<String, Object>> rows = new ArrayList<>();
        Map<String, List<Object>> columns = new HashMap<>();
        for (String column : queryResult.getColumns()) {
            columns.put(column, new ArrayList<>());
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
         * Return a column identified by its name.
         *
         * @param <T> The expected type.
         * @return All columns.
         */
        public <T> List<T> getColumn(String name) {
            return (List<T>) columns.get(name);
        }
    }
}
