package com.buschmais.jqassistant.scanner.test;

import com.buschmais.jqassistant.scanner.api.ClassScanner;
import com.buschmais.jqassistant.scanner.impl.ClassScannerImpl;
import com.buschmais.jqassistant.store.api.QueryResult;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.impl.EmbeddedGraphStore;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractScannerIT {

    protected Store store;

    @Before
    public void startStore() {
        store = new EmbeddedGraphStore("target/jqassistant/" + this.getClass().getSimpleName());
        store.start();
        store.beginTransaction();
        store.reset();
        store.endTransaction();
    }

    @After
    public void stopStore() {
        store.stop();
    }

    protected ClassScanner getScanner() {
        return new ClassScannerImpl(store, getScanListener());
    }

    /**
     * Return the {@link com.buschmais.jqassistant.scanner.impl.ClassScannerImpl.ScanListener} to be used for scanning.
     * <p>The default implementation returns a listener without any functionality, a class may override this method to return a listener implementing specific behavior.</p>
     *
     * @return The {@link com.buschmais.jqassistant.scanner.impl.ClassScannerImpl.ScanListener}.
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
        store.beginTransaction();
        getScanner().scanClasses(classes);
        store.endTransaction();
    }

    /**
     * Executes a CYPHER query and returns a {@link com.buschmais.jqassistant.scanner.test.AbstractScannerIT.TestResult}.
     *
     * @param query The query.
     * @return The  {@link com.buschmais.jqassistant.scanner.test.AbstractScannerIT.TestResult}.
     */
    protected TestResult executeQuery(String query) {
        QueryResult queryResult = store.executeQuery(query);
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
