package com.buschmais.jqassistant.core.report.api.model;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Rule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.ToString;

/**
 * The result of an executed {@link Rule}.
 *
 * @param <T>
 *            The rule type.
 *
 * @see Rule
 */
@Builder
@AllArgsConstructor
@ToString
public class Result<T extends ExecutableRule> {

    /**
     * The defined status for the result of a rule.
     */
    public enum Status {
        SUCCESS, FAILURE, WARNING, SKIPPED;
    }

    /**
     * The executed rule.
     */
    private T rule;

    private Status status;

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

    public T getRule() {
        return rule;
    }

    public Status getStatus() {
        return status;
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
