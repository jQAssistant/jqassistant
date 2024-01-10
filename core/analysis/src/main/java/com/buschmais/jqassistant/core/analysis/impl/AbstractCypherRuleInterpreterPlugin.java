package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

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
import static java.util.Optional.empty;
import static java.util.Optional.of;

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
            getColumns(executableRule.getId(), columnNames, primaryColumn, rowObject, context).ifPresent(
                columns -> rows.add(context.toRow(executableRule, columns)));
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

    private static Optional<Map<String, Column<?>>> getColumns(String ruleId, List<String> columnNames, String primaryColumn,
        Query.Result.CompositeRowObject rowObject, AnalyzerContext context) {
        Map<String, Column<?>> columns = new LinkedHashMap<>();
        for (String columnName : columnNames) {
            Object columnValue = rowObject.get(columnName, Object.class);
            if (isSuppressed(columnName, columnValue, ruleId, primaryColumn)) {
                return empty();
            }
            columns.put(columnName, context.toColumn(columnValue));
        }
        return of(columns);
    }

    /**
     * Verifies if the given column indicates that the row shall be suppressed.
     * <p>
     * The primary column is checked if it contains a suppression that matches the
     * current rule id.
     *
     * @param columnName
     *     The column name.
     * @param columnValue
     *     The column value.
     * @param ruleId
     *     The rule id.
     * @param primaryColumn
     *     The name of the primary column.
     * @return <code>true</code> if the row shall be suppressed.
     */
    private static boolean isSuppressed(String columnName, Object columnValue, String ruleId, String primaryColumn) {
        if (columnValue != null && Suppress.class.isAssignableFrom(columnValue.getClass())) {
            Suppress suppress = (Suppress) columnValue;
            String suppressColumn = suppress.getSuppressColumn();
            if ((suppressColumn != null && suppressColumn.equals(columnName)) || primaryColumn.equals(columnName)) {
                String[] suppressIds = suppress.getSuppressIds();
                for (String suppressId : suppressIds) {
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
