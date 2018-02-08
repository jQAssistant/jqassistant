package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleLanguagePlugin;
import com.buschmais.jqassistant.core.analysis.api.rule.ExecutableRule;
import com.buschmais.jqassistant.core.analysis.api.rule.RuleException;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;
import com.buschmais.xo.api.Query;

import static java.util.Collections.singletonList;

public class CypherLanguagePlugin implements RuleLanguagePlugin {

    private static final Set<String> LANGUAGES = new HashSet<>(singletonList("cypher"));

    @Override
    public Set<String> getLanguages() {
        return LANGUAGES;
    }

    @Override
    public <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Map<String, Object> parameters, Severity severity, AnalyzerContext context)
            throws RuleException {
        String cypher = executableRule.getExecutable().getSource();
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
            Result.Status status = context.verify(executableRule, columnNames, rows, context);
            return new Result<>(executableRule, status, severity, columnNames, rows);
        } catch (Exception e) {
            throw new RuleException("Cannot execute query for rule '" + executableRule + "'.", e);
        }
    }

}
