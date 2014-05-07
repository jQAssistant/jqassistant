package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule;

/**
 * The result of a
 * {@link com.buschmais.jqassistant.core.analysis.api.rule.Query} using an
 * {@link com.buschmais.jqassistant.core.analysis.api.rule.AbstractRule}.
 */
public class Result<T extends AbstractRule> {

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
