package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.store.api.Store;

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
    <T> Column toColumn(T value);

    /**
     * Create a result {@link Row} from a map of columns.
     *
     * @param rule
     * @param columns
     *     The columns.
     * @return The {@link Row}
     */
    Row toRow(ExecutableRule<?> rule, Map<String, Column<?>> columns);

    /**
     * Verifies if the Row shall be suppressed.
     * <p>
     * The primary column is checked if it contains a suppression that matches the
     * current rule id.
     *
     * @param executableRule
     *     The {@link ExecutableRule}.
     * @param primaryColumn
     *     The name of the primary column.
     * @param row
     *     The {@link Row}.
     * @return <code>true</code> if the row shall be suppressed.
     */
    <T extends ExecutableRule<?>> boolean isSuppressed(T executableRule, String primaryColumn, Row row);

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

    Result.Status getStatus(VerificationResult verificationResult, Severity severity);
}
