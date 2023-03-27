package com.buschmais.jqassistant.core.analysis.impl;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.report.api.model.Column;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.report.api.model.Row;
import com.buschmais.jqassistant.core.report.api.model.Suppress;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.xo.api.Query;

import lombok.extern.slf4j.Slf4j;

import static com.buschmais.jqassistant.core.report.api.model.Result.Status;
import static java.util.Collections.unmodifiableList;

/**
 * Abstract base class for {@link RuleInterpreterPlugin}s executing cypher
 * queries.
 * <p>
 * The
 */
@Slf4j
public abstract class AbstractCypherRuleInterpreterPlugin implements RuleInterpreterPlugin {

    protected <T extends ExecutableRule<?>> Result<T> execute(String cypher, T executableRule, Map<String, Object> parameters, Severity severity,
        AnalyzerContext context) throws RuleException {
        log.debug("Executing query '" + cypher + "' with parameters [" + parameters + "]");
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
        List<Row> rows = new LinkedList<>();
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
            Map<String, Column<?>> columns = new LinkedHashMap<>();
            for (String columnName : columnNames) {
                    Object value = rowObject.get(columnName, Object.class);
                    columns.put(columnName, context.toColumn(value));
            }
                if (!isSuppressedRow(executableRule.getId(), columns, primaryColumn)) {
                    rows.add(context.toRow(executableRule, columns));
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
     * @param columns
     *     The columns.
     * @param primaryColumn
     *     The name of the primary column.
     * @return <code>true</code> if the row shall be suppressed.
     */
    private boolean isSuppressedRow(String ruleId, Map<String, Column<?>> columns, String primaryColumn) {
        Column column = columns.get(primaryColumn);
        if (column != null) {
            Object value = column.getValue();
            if (value != null && Suppress.class.isAssignableFrom(value.getClass())) {
                Suppress suppress = (Suppress) value;
                for (String suppressId : suppress.getSuppressIds()) {
                    if (ruleId.equals(suppressId)) {
                        return true;
                    }
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
    protected <T extends ExecutableRule<?>> Status getStatus(T executableRule, Severity severity, List<String> columnNames, List<Row> rows,
        AnalyzerContext context) throws RuleException {
        return context.verify(executableRule, severity, columnNames, rows);
    }

}
