package com.buschmais.jqassistant.core.store.api;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;

/**
 * Represents the result of a CYPHER query.
 */
public class QueryResult implements Closeable {

    /**
     * Describes one row of a query result containing named columns and value.
     */
    public static class Row {

        private Map<String, Object> row;

        public Row(Map<String, Object> row) {
            this.row = row;
        }

		@SuppressWarnings("unchecked")
		public <T> T get(String column) {
            return (T) row.get(column);
        }

        public Map<String, Object> get() {
            return row;
        }
    }

    /**
     * The column names returned by the query.
     */
    private final List<String> columns;

    /**
     * The Iterable which can be used to scroll through the rows returned by the
     * query.
     * <p>
     * Where applicable the values of a row are transformed to instances of the
     * corresponding classes, e.g. nodes will be made available as instances of
     * the according {@link com.buschmais.jqassistant.core.store.api.descriptor.AbstractDescriptor}s.
     * </p>
     */
    private final Iterable<Row> rows;

    /**
     * Constructor.
     *
     * @param columns A list containing the names of the returned columns.
     * @param rows    The rows.
     */
    public QueryResult(List<String> columns, Iterable<Row> rows) {
        this.columns = columns;
        this.rows = rows;
    }

    /**
     * Return the column names.
     *
     * @return The column names.
     */
    public List<String> getColumns() {
        return columns;
    }

    /**
     * Return the {@link Iterable} to be used to scroll through the rows.
     *
     * @return The {@link Iterable} to be used to scroll through the rows.
     */
    public Iterable<Row> getRows() {
        return rows;
    }

    @Override
    public void close() {
        if (this.rows instanceof Closeable) {
            IOUtils.closeQuietly((Closeable) rows);
        }
    }
}
