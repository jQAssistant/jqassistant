package com.buschmais.jqassistant.core.report.api.model;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.Rule;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

import lombok.*;

import static lombok.AccessLevel.PRIVATE;

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
    // TODO Consider moving this to a separate class as this enum is also used to report the status of a row.
    @Getter
    @RequiredArgsConstructor(access = PRIVATE)
    public enum Status {
        FAILURE(0),
        WARNING(1),
        SUCCESS(2),
        SKIPPED(3);

        private final Integer level;
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
