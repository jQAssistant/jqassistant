package com.buschmais.jqassistant.store.api.model;

import org.apache.commons.io.IOUtils;

import java.io.Closeable;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a CYPHER query.
 */
public class QueryResult implements AutoCloseable, Closeable {

    /**
     * The column names returned by the query.
     */
    private final List<String> columns;

    /**
     * The Iterable which can be used to scroll through the rows returned by the
     * query.
     * <p>
     * Each row contains a {@link Map} where the key is one of the column names
     * as defined by {@link #columns} and the value is the value returned by the
     * query. Where applicable the values are transformed to instances of the
     * corresponding classes, e.g. nodes will be made available as instances of
     * the according {@link AbstractDescriptor}s.
     * </p>
     */
    private final Iterable<Map<String, Object>> rows;

    public QueryResult(List<String> columns, Iterable<Map<String, Object>> rows) {
        super();
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
    public Iterable<Map<String, Object>> getRows() {
        return rows;
    }

    @Override
    public void close() {
        if (this.getRows() instanceof Closeable) {
            IOUtils.closeQuietly((Closeable) rows);

        }
    }
}
