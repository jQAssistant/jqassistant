package com.buschmais.jqassistant.core.analysis.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AbstractRuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.xo.api.Query;

import static com.buschmais.jqassistant.core.analysis.api.Result.Status;

/**
 * Abstract base class for {@link RuleLanguagePlugin}s executing cypher queries.
 */
public abstract class AbstractCypherLanguagePlugin extends AbstractRuleLanguagePlugin {

    protected <T extends ExecutableRule<?>> Result<T> execute(String cypher, T executableRule, Map<String, Object> parameters, Severity severity,
            AnalyzerContext context) throws RuleException {
        List<Map<String, Object>> rows = new ArrayList<>();
        context.getLogger().debug("Executing query '" + cypher + "' with parameters [" + parameters + "]");
        List<String> columnNames = null;
        try (Query.Result<Query.Result.CompositeRowObject> compositeRowObjects = context.getStore().executeQuery(cypher, parameters)) {
            for (Query.Result.CompositeRowObject rowObject : compositeRowObjects) {
                if (columnNames == null) {
                    columnNames = new ArrayList<>(rowObject.getColumns());
                }
                Map<String, Object> row = new LinkedHashMap<>();
                for (String columnName : columnNames) {
                    row.put(columnName, rowObject.get(columnName, Object.class));
                }
                rows.add(row);
            }
        } catch (Exception e) {
            throw new RuleException("Cannot execute query for rule '" + executableRule + "'.", e);
        }
        Status status = getStatus(executableRule, columnNames, rows, context);
        return Result.<T>builder().rule(executableRule).status(status).severity(severity).columnNames(columnNames).rows(rows).build();
    }

    /**
     * Evaluate the status of the result, may be overridden by sub-classes.
     *
     * @param executableRule
     *            The {@link ExecutableRule}.
     * @param columnNames
     *            The column names.
     * @param rows
     *            The rows.
     * @param context
     *            The {@link AnalyzerContext}.
     * @param <T>
     *            The rule type.
     * @return The {@link Status}.
     * @throws RuleException
     *             If evaluation fails.
     */
    protected <T extends ExecutableRule<?>> Status getStatus(T executableRule, List<String> columnNames, List<Map<String, Object>> rows,
            AnalyzerContext context) throws RuleException {
        return context.verify(executableRule, columnNames, rows, context);
    }

}
