package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.Rule;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

/**
 * The result of an executed {@link Rule}.
 * 
 * @param <T>
 *            The rule type.
 */
public class Result<T extends Rule> {

    /**
     * The executed rule.
     */
    private T rule;

    /**
     * The effective severity.
     */
    private Severity severity;

    /**
     * The list of returned columns.
     */
    private List<String> columnNames;

    /**
     * The returned rows.
     */
    private List<Map<String, Object>> rows;

    /**
     * Constructor.
     * 
     * @param rule
     *            The executed rule.
     * @param severity
     *            The effective severity.
     * @param columnNames
     *            The names of the columns per row.
     * @param rows
     *            The rows.
     */
    public Result(T rule, Severity severity, List<String> columnNames, List<Map<String, Object>> rows) {
        this.rule = rule;
        this.severity = severity;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public T getRule() {
        return rule;
    }

    public Severity getSeverity() {
        return severity;
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
