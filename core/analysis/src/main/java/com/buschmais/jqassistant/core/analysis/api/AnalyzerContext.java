package com.buschmais.jqassistant.core.analysis.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.impl.AnalyzerContextImpl;
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
     * @param context
     *            The {@link AnalyzerContextImpl}.
     * @return The status.
     * @throws RuleException
     *             If no valid verification strategy can be found.
     */
    <T extends ExecutableRule> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows, AnalyzerContext context)
            throws RuleException;

}
