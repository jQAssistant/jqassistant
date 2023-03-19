package com.buschmais.jqassistant.core.analysis.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Suppress;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.xo.api.Query;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status;
import static java.util.Collections.unmodifiableList;

/**
 * Abstract base class for {@link RuleInterpreterPlugin}s executing cypher
 * queries.
 * <p>
 * The
 */
public abstract class AbstractCypherRuleInterpreterPlugin implements RuleInterpreterPlugin {

    protected <T extends ExecutableRule<?>> Result<T> execute(String cypher, T executableRule, Map<String, Object> parameters, Severity severity,
        AnalyzerContext context) throws RuleException {
        context.getLogger()
            .debug("Executing query '" + cypher + "' with parameters [" + parameters + "]");
        try (Query.Result<Query.Result.CompositeRowObject> compositeRowObjects = context.getStore()
            .executeQuery(cypher, parameters)) {
            return context.getStore()
                .requireTransaction(() -> getResult(executableRule, severity, context, compositeRowObjects));
        } catch (Exception e) {
            throw new RuleException("Cannot execute query for rule '" + executableRule + "'.", e);
        }
    }

    private <T extends ExecutableRule<?>> Result<T> getResult(T executableRule, Severity severity, AnalyzerContext context,
        Query.Result<Query.Result.CompositeRowObject> compositeRowObjects) throws RuleException {
        List<Map<String, Object>> rows = new LinkedList<>();
        String primaryColumn = null;
        List<String> columnNames = null;
        for (Query.Result.CompositeRowObject rowObject : compositeRowObjects) {
            if (columnNames == null) {
                columnNames = unmodifiableList(rowObject.getColumns());
                primaryColumn = executableRule.getReport()
                    .getPrimaryColumn();
                if (primaryColumn == null) {
                    primaryColumn = columnNames.get(0);
                }
            }
            Map<String, Object> row = new LinkedHashMap<>();
            for (String columnName : columnNames) {
                row.put(columnName, rowObject.get(columnName, Object.class));
            }
            if (!isSuppressedRow(executableRule.getId(), row, primaryColumn)) {
                rows.add(row);
            }
        }
        Status status = getStatus(executableRule, severity, columnNames, rows, context);
        return Result.<T>builder()
            .rule(executableRule)
            .status(status)
            .severity(severity)
            .columnNames(columnNames)
            .rows(rows)
            .build();
    }

    /**
     * Verifies if the given row shall be suppressed.
     * <p>
     * The primary column is checked if it contains a suppression that matches the
     * current rule id.
     *
     * @param ruleId
     *     The rule id.
     * @param row
     *     The row.
     * @param primaryColumn
     *     The name of the primary column.
     * @return <code>true</code> if the row shall be suppressed.
     */
    private boolean isSuppressedRow(String ruleId, Map<String, Object> row, String primaryColumn) {
        Object primaryValue = row.get(primaryColumn);
        if (primaryValue != null && Suppress.class.isAssignableFrom(primaryValue.getClass())) {
            Suppress suppress = (Suppress) primaryValue;
            for (String suppressId : suppress.getSuppressIds()) {
                if (ruleId.equals(suppressId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Evaluate the status of the result, may be overridden by sub-classes.
     *
     * @param executableRule
     *     The {@link ExecutableRule}.
     * @param severity
     *     The effective {@link Severity}.
     * @param columnNames
     *     The column names.
     * @param rows
     *     The rows.
     * @param context
     *     The {@link AnalyzerContext}.
     * @param <T>
     *     The rule type.
     * @return The {@link Status}.
     * @throws RuleException
     *     If evaluation fails.
     */
    protected <T extends ExecutableRule<?>> Status getStatus(T executableRule, Severity severity, List<String> columnNames, List<Map<String, Object>> rows,
        AnalyzerContext context) throws RuleException {
        return context.verify(executableRule, severity, columnNames, rows);
    }

}
