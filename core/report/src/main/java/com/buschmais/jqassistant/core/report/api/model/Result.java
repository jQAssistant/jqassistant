package com.buschmais.jqassistant.core.report.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Rule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import lombok.ToString;

/**
 * The result of an executed {@link Rule}.
 *
 * @param <T>
 *     The rule type.
 * @see Rule
 */
@Builder
@Getter
@ToString
public class Result<T extends ExecutableRule> {

    /**
     * The defined status for the result of a rule.
     */
    public enum Status {
        SUCCESS,
        FAILURE,
        WARNING,
        SKIPPED
    }

    /**
     * The executed rule.
     */
    private final T rule;

    private final VerificationResult verificationResult;

    private final Status status;

    /**
     * The effective severity.
     */
    private final Severity severity;

    /**
     * The list of returned columns.
     */
    private final List<String> columnNames;

    /**
     * The returned rows.
     */
    @Singular
    private final List<Row> rows;

    /**
     * @deprecated Use provided {@link #builder()}.
     */
    @Deprecated(since = "2.6.0")
    public Result(T rule, Status status, Severity severity, List<String> columnNames, List<Row> rows) {
        this(rule, VerificationResult.builder()
            .success(Status.SUCCESS.equals(status))
            .rowCount(rows.size())
            .build(), status, severity, columnNames, rows);
    }

    private Result(T rule, VerificationResult verificationResult, Status status, Severity severity, List<String> columnNames, List<Row> rows) {
        this.rule = rule;
        this.verificationResult = verificationResult;
        this.status = status;
        this.severity = severity;
        this.columnNames = columnNames;
        this.rows = rows;
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
