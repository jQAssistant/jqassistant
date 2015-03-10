package com.buschmais.jqassistant.core.analysis.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.buschmais.jqassistant.core.analysis.api.*;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOException;

/**
 * Implementation of a rule visitor for analysis execution.
 */
public class AnalyzerVisitor extends AbstractRuleVisitor {

    /**
     * Defines the available variables for scripts.
     */
    private enum ScriptVariable {

        STORE, RULE, SEVERITY;

        String getVariableName() {
            return name().toLowerCase();
        }
    }

    private RuleSet ruleSet;
    private Store store;
    private AnalysisListener reportWriter;
    private Console console;
    private ScriptEngineManager scriptEngineManager;
    private Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param ruleSet
     *            The rule set to execute.
     * @param store
     *            The store.
     * @param reportWriter
     *            The report writer.
     * @param console
     *            The console
     */
    public AnalyzerVisitor(RuleSet ruleSet, Store store, AnalysisListener reportWriter, Console console) {
        this.ruleSet = ruleSet;
        this.store = store;
        this.reportWriter = reportWriter;
        this.console = console;
        this.scriptEngineManager = new ScriptEngineManager();
        registerVerificationStrategy(new RowCountVerificationStrategy());
        registerVerificationStrategy(new AggregationVerificationStrategy());
    }

    private void registerVerificationStrategy(VerificationStrategy verificationStrategy) {
        verificationStrategies.put(verificationStrategy.getVerificationType(), verificationStrategy);
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
    private <T extends ExecutableRule> Result<T> execute(RuleSet ruleSet, T executable, Severity severity) throws AnalysisException {
        Script script = executable.getScript();
        if (script != null) {
            return executeScript(script, executable, severity);
        } else {
            return executeCypher(ruleSet, executable, severity);
        }
    }

    /**
     * Execute the cypher query of a rule.
     * 
     * @param ruleSet
     *            The rule set.
     * @param executable
     *            The executable.
     * @param severity
     *            The severity.
     * @param <T>
     *            The rule type.
     * @return The result.
     * @throws AnalysisException
     *             If execution fails.
     */
    private <T extends ExecutableRule> Result<T> executeCypher(RuleSet ruleSet, T executable, Severity severity) throws AnalysisException {
        String queryTemplateId = executable.getTemplateId();
        String cypher;
        if (queryTemplateId != null) {
            Template template = ruleSet.getTemplates().get(queryTemplateId);
            if (template == null) {
                throw new AnalysisException("Cannot find query template " + queryTemplateId);
            }
            cypher = template.getCypher();
        } else {
            cypher = executable.getCypher();
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Query.Result<Query.Result.CompositeRowObject> compositeRowObjects = executeQuery(cypher, executable.getParameters())) {
            List<String> columnNames = null;
            for (Query.Result.CompositeRowObject rowObject : compositeRowObjects) {
                if (columnNames == null) {
                    columnNames = new ArrayList<>(rowObject.getColumns());
                }
                Map<String, Object> row = new HashMap<>();
                for (String columnName : columnNames) {
                    row.put(columnName, rowObject.get(columnName, Object.class));
                }
                rows.add(row);
            }
            Result.Status status = verify(executable, columnNames, rows);
            return new Result<>(executable, status, severity, columnNames, rows);
        } catch (Exception e) {
            throw new AnalysisException("Cannot execute query.", e);
        }
    }

    /**
     * Verifies the rows returned by a cypher query for an executable.
     * 
     * @param executable
     *            The executable.
     * @param columnNames
     *            The column names.
     * @param rows
     *            The rows.
     * @param <T>
     *            The type of the executable.
     * @return The status.
     * @throws AnalysisException
     *             If no valid verification strategy can be found.
     */
    private <T extends ExecutableRule> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows) throws AnalysisException {
        Verification verification = executable.getVerification();
        VerificationStrategy strategy = verificationStrategies.get(verification.getClass());
        if (strategy == null) {
            throw new AnalysisException("Result verification not supported: " + verification.getClass().getName());
        }
        return strategy.verify(executable, verification, columnNames, rows);
    }

    /**
     * Execute an analysis script
     * 
     * @param script
     *            The script.
     * @param severity
     *            The severity.
     * @param <T>
     *            The result type.
     * @return The result.
     * @throws AnalysisException
     *             If script execution fails.
     */
    private <T extends ExecutableRule> Result<T> executeScript(Script script, T executable, Severity severity) throws AnalysisException {
        String language = script.getLanguage();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);
        if (scriptEngine == null) {
            List<String> availableLanguages = new ArrayList<>();
            for (ScriptEngineFactory factory : scriptEngineManager.getEngineFactories()) {
                availableLanguages.addAll(factory.getNames());
            }
            throw new AnalysisException("Cannot resolve scripting engine for '" + language + "', available languages are " + availableLanguages);
        }
        scriptEngine.put(ScriptVariable.STORE.getVariableName(), store);
        scriptEngine.put(ScriptVariable.RULE.getVariableName(), executable);
        scriptEngine.put(ScriptVariable.SEVERITY.getVariableName(), severity);
        Object scriptResult;
        try {
            scriptResult = scriptEngine.eval(script.getSource());
        } catch (ScriptException e) {
            throw new AnalysisException("Cannot execute script.", e);
        }
        if (!(scriptResult instanceof Result)) {
            throw new AnalysisException("Script returned an invalid result language, expected " + Result.class.getName() + " but got " + scriptResult);
        }
        return Result.class.cast(scriptResult);
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
