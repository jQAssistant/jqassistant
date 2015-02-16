package com.buschmais.jqassistant.core.analysis.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.Console;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOException;

public class AnalyzerVisitor extends AbstractRuleVisitor {

    private RuleSet ruleSet;
    private Store store;
    private AnalysisListener reportWriter;
    private Console console;

    public AnalyzerVisitor(RuleSet ruleSet, Store store, AnalysisListener reportWriter, Console console) {
        this.ruleSet = ruleSet;
        this.store = store;
        this.reportWriter = reportWriter;
        this.console = console;
    }

    @Override
    public void visitConcept(Concept concept, Severity severity) throws AnalysisException {
        try {
            store.beginTransaction();
            ConceptDescriptor conceptDescriptor = store.find(ConceptDescriptor.class, concept.getId());
            if (conceptDescriptor == null) {
                console.info("Applying concept '" + concept.getId() + "'.");
                reportWriter.beginConcept(concept);
                reportWriter.setResult(execute(ruleSet, concept, severity));
                conceptDescriptor = store.create(ConceptDescriptor.class);
                conceptDescriptor.setId(concept.getId());
                reportWriter.endConcept();
            }
            store.commitTransaction();
        } catch (XOException e) {
            store.rollbackTransaction();
            throw new AnalysisException("Cannot apply concept " + concept.getId(), e);
        }
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity severity) throws AnalysisException {
        console.info("Validating constraint '" + constraint.getId() + "' with severity: '" + constraint.getSeverity() + "'.");
        try {
            store.beginTransaction();
            reportWriter.beginConstraint(constraint);
            reportWriter.setResult(execute(ruleSet, constraint, severity));
            reportWriter.endConstraint();
            store.commitTransaction();
        } catch (XOException e) {
            store.rollbackTransaction();
            throw new AnalysisException("Cannot validate constraint " + constraint.getId(), e);
        }
    }

    @Override
    public void beforeGroup(Group group) throws AnalysisException {
        console.info("Executing group '" + group.getId() + "'");
        store.beginTransaction();
        reportWriter.beginGroup(group);
        store.commitTransaction();
        Map<String, Severity> concepts = group.getConcepts();
    }

    @Override
    public void afterGroup(Group group) throws AnalysisException {
        store.beginTransaction();
        reportWriter.endGroup();
        store.commitTransaction();
    }

    /**
     * Run the given executable and return a result which can be passed to a
     * report writer.
     *
     * @param executable
     *            The executable.
     * @param <T>
     *            The types of the executable.
     * @return The result.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If query execution fails.
     */
    private <T extends AbstractRule> Result<T> execute(RuleSet ruleSet, T executable, Severity severity) throws AnalysisException {
        List<Map<String, Object>> rows = new ArrayList<>();
        String queryTemplateId = executable.getTemplateId();
        String cypher;
        if (queryTemplateId != null) {
            Template template = ruleSet.getQueryTemplates().get(queryTemplateId);
            if (template == null) {
                throw new AnalysisException("Cannot find query template " + queryTemplateId);
            }
            cypher = template.getCypher();
        } else {
            cypher = executable.getCypher();
        }
        try (com.buschmais.xo.api.Query.Result<Query.Result.CompositeRowObject> compositeRowObjects = executeQuery(cypher, executable.getParameters())) {
            List<String> columns = null;
            for (Query.Result.CompositeRowObject rowObject : compositeRowObjects) {
                if (columns == null) {
                    columns = new ArrayList<>(rowObject.getColumns());
                }
                Map<String, Object> row = new HashMap<>();
                for (String columnName : columns) {
                    row.put(columnName, rowObject.get(columnName, Object.class));
                }
                rows.add(row);
            }
            return new Result<>(executable, severity, columns, rows);
        } catch (Exception e) {
            throw new AnalysisException("Cannot execute query.", e);
        }
    }

    /**
     * Execute the given query.
     *
     * @param cypher
     *            The query.
     * @param parameters
     *            The parameters.
     * @return The query result.
     */
    private com.buschmais.xo.api.Query.Result<Query.Result.CompositeRowObject> executeQuery(String cypher, Map<String, Object> parameters) {
        console.debug("Executing query '" + cypher + "' with parameters [" + parameters + "]");
        return store.executeQuery(cypher, parameters);
    }
}
