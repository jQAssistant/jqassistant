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
import com.buschmais.jqassistant.core.report.api.model.VerificationResult;
import com.buschmais.jqassistant.core.rule.api.model.ExecutableRule;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.xo.api.Query;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

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
        String primaryColumn = executableRule.getReport()
            .getPrimaryColumn();
        List<String> columnNames = null;
        for (Query.Result.CompositeRowObject rowObject : compositeRowObjects) {
            if (columnNames == null) {
                columnNames = unmodifiableList(rowObject.getColumns());
                if (primaryColumn == null) {
                    primaryColumn = columnNames.get(0);
                }
            }
            Row row = getColumns(executableRule, columnNames, rowObject, context);
            if (!context.isSuppressed(executableRule, primaryColumn, row)) {
                rows.add(row);
            }
        }
        VerificationResult verificationResult = context.verify(executableRule, columnNames, rows);
        Status status = context.getStatus(verificationResult, severity);
        return Result.<T>builder()
            .rule(executableRule)
            .verificationResult(verificationResult)
            .status(status)
            .severity(severity)
            .columnNames(columnNames)
            .rows(rows)
            .build();
    }

    private Row getColumns(ExecutableRule<?> rule, List<String> columnNames, Query.Result.CompositeRowObject rowObject, AnalyzerContext context) {
        Map<String, Column<?>> columns = new LinkedHashMap<>();
        for (String columnName : columnNames) {
            Object columnValue = rowObject.get(columnName, Object.class);
            columns.put(columnName, context.toColumn(columnValue));
        }

        Map<String, Column<?>> keyColumns = new LinkedHashMap<>();
        if(StringUtils.isNotEmpty(rule.getReport().getKeyColumns())) {
            for (String columnName : rule.getReport()
                    .getKeyColumns()
                    .split("\\s*,\\s*")) {
                Object columnValue = rowObject.get(columnName, Object.class);
                keyColumns.put(columnName, context.toColumn(columnValue));
            }
        }
        return context.toRow(rule, columns, keyColumns);
    }

}
