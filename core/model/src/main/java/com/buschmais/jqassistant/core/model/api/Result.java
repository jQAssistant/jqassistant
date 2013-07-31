package com.buschmais.jqassistant.core.model.api;

import com.buschmais.jqassistant.core.model.api.rules.AbstractExecutable;

import java.util.List;
import java.util.Map;

/**
 * The result of a {@link Query} using an {@link  AbstractExecutable}.
 */
public class Result<T extends AbstractExecutable> {

    private T executable;

    private List<String> columnNames;

    private List<Map<String, Object>> rows;

    public Result(T executable, List<String> columnNames, List<Map<String, Object>> rows) {
        this.executable = executable;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public T getExecutable() {
        return executable;
    }

    public List<String> getColumnNames() {
        return columnNames;
    }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
