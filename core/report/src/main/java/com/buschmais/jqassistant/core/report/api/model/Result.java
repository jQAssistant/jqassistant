package com.buschmais.jqassistant.core.report.api.model;

import java.util.List;
import java.util.Optional;

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
     * The primary column;
     */
    private final Optional<String> primaryColumn;

    /**
     * The returned rows.
     */
    @Singular
    private final List<Row> rows;

    public boolean isEmpty() {
        return rows.isEmpty();
    }
}
