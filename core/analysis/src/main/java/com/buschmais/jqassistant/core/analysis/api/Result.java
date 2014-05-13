package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.Rule;

/**
 * The result of a
 * {@link com.buschmais.jqassistant.core.analysis.api.rule.Query} using an
 * {@link com.buschmais.jqassistant.core.analysis.api.rule.Rule}.
 */
public class Result<T extends Rule> {

    private T rule;

    private List<String> columnNames;

    private List<Map<String, Object>> rows;

    public Result(T rule, List<String> columnNames, List<Map<String, Object>> rows) {
        this.rule = rule;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public T getRule() {
        return rule;
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
