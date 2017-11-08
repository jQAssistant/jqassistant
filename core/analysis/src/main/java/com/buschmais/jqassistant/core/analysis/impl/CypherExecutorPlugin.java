package com.buschmais.jqassistant.core.analysis.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleExecutorPlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.xo.api.Query;

public class CypherExecutorPlugin implements RuleExecutorPlugin<CypherExecutable> {

    @Override
    public Class<CypherExecutable> getType() {
        return CypherExecutable.class;
    }

    @Override
    public <T extends ExecutableRule<CypherExecutable>> Result<T> execute(T executableRule, Map<String, Object> parameters, Severity severity,
            AnalyzerContext context) throws RuleException {
        String cypher = executableRule.getExecutable().getStatement();
        List<Map<String, Object>> rows = new ArrayList<>();
        context.getLogger().debug("Executing query '" + cypher + "' with parameters [" + parameters + "]");
        try (Query.Result<Query.Result.CompositeRowObject> compositeRowObjects = context.getStore().executeQuery(cypher, parameters)) {
            List<String> columnNames = null;
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
            Result.Status status = verify(executableRule, columnNames, rows, context);
            return new Result<>(executableRule, status, severity, columnNames, rows);
        } catch (Exception e) {
            throw new RuleException("Cannot execute query for rule '" + executableRule + "'.", e);
        }
    }

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
    private <T extends ExecutableRule> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows, AnalyzerContext context)
            throws RuleException {
        Verification verification = executable.getVerification();
        VerificationStrategy strategy = context.getVerificationStrategies().get(verification.getClass());
        if (strategy == null) {
            throw new RuleException("Result verification not supported: " + verification.getClass().getName());
        }
        return strategy.verify(executable, verification, columnNames, rows);
    }

}
