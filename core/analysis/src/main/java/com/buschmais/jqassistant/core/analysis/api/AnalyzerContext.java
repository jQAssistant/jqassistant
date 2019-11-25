package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.rule.api.model.Verification;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

/**
 * Provides access to infrastructure for analysis..
 */
public interface AnalyzerContext {

    /**
     * Return the {@link Store}.
     *
     * @return The {@link Store}.
     */
    Store getStore();

    /**
     * Return the {@link Logger} for emitting user warnings/errors..
     *
     * @return The {@link Logger}.
     */
    Logger getLogger();

    /**
     * Verifies the rows returned by a cypher query for an executable.
     *
     * @param <T>
     *            The type of the executable.
     * @param executable
     *            The executable.
     * @param columnNames
     *            The column names.
     * @param rows
     *            The rows.
     * @return The status.
     * @throws RuleException
     *             If no valid verification strategy can be found.
     */
    <T extends ExecutableRule<?>> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows) throws RuleException;

    /**
     * Verifies the rows returned by a cypher query for an executable.
     *
     * @param <T>
     *            The type of the executable.
     * @param executable
     *            The executable.
     * @param columnNames
     *            The column names.
     * @param rows
     *            The rows.
     * @param verification
     *            The {@link Verification} to perform.
     * @return The status.
     * @throws RuleException
     *             If no valid verification strategy can be found.
     */
    <T extends ExecutableRule<?>> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows, Verification verification)
            throws RuleException;

    /**
     * Return a Result.ResultBuilder based on a {@link ExecutableRule} and
     * {@link Severity}.
     *
     * @param rule
     *            The rule.
     * @param severity
     *            The {@link Severity} the {@link ExecutableRule} has been executed
     *            with.
     * @param <T>
     *            The rule type.
     * @return The ResultBuilder.
     */
    <T extends ExecutableRule<?>> Result.ResultBuilder<T> resultBuilder(T rule, Severity severity);

}
