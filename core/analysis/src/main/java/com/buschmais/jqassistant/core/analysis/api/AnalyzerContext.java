package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.store.api.Store;

import static java.util.Optional.empty;

/**
 * Provides access to infrastructure for analysis..
 */
public interface AnalyzerContext {

    /**
     * Return the plugin {@link ClassLoader}.
     *
     * @return The plugin {@link ClassLoader}
     */
    ClassLoader getClassLoader();

    /**
     * Return the {@link Store}.
     *
     * @return The {@link Store}.
     */
    Store getStore();

    /**
     * Create a result {@link Column} from a value.
     *
     * @param value
     *     The value.
     * @param <T>
     *     The value type.
     * @return The {@link Column}.
     */
    <T> Column<?> toColumn(T value);

    /**
     * Create a result {@link Row} from a map of columns.
     * Checks for suppression and sets the according values.
     *
     * @param rule
     *     The {@link ExecutableRule}
     * @param columns
     *     The columns.
     * @return The {@link Row}
     */
    @Deprecated
    default Row toRow(ExecutableRule<?> rule, Map<String, Column<?>> columns) {
        return toRow(rule, columns, empty());
    }

    /**
     * Create a result {@link Row} from a map of columns.
     * Checks for suppression and sets the according values.
     *
     * @param rule
     *     The {@link ExecutableRule}
     * @param columns
     *     The columns.
     * @param primaryColumn
     *     The name of the primary column
     * @return The {@link Row}
     */
    Row toRow(ExecutableRule<?> rule, Map<String, Column<?>> columns, Optional<String> primaryColumn);

    /**
     * Verifies the rows returned by a cypher query for an executable.
     *
     * @param <T>
     *     The type of the executable.
     * @param executable
     *     The executable.
     * @param columnNames
     *     The column names.
     * @param rows
     *     The rows.
     * @return The status.
     * @throws RuleException
     *     If no valid verification strategy can be found.
     */
    <T extends ExecutableRule<?>> VerificationResult verify(T executable, List<String> columnNames, List<Row> rows) throws RuleException;

    /**
     * Get the status of an executed rule.
     *
     * @param verificationResult
     *     The {@link VerificationResult}.
     * @param severity
     *     The {@link Severity}.
     * @return The {@link com.buschmais.jqassistant.core.report.api.model.Result.Status}.
     */
    Result.Status getStatus(VerificationResult verificationResult, Severity severity);

    /**
     * Identify the primary column of an executed rule.
     *
     * @param rule
     *     The rule.
     * @param columnNames
     *     The column names from the rule result.
     * @return The optional primary columns name.
     */
    Optional<String> getPrimaryColumn(ExecutableRule<?> rule, List<String> columnNames);
}
