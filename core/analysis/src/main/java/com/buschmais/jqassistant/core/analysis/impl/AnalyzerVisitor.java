package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.AnalysisListener;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.VerificationStrategy;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.XOException;

import org.slf4j.Logger;


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
    private boolean executeAppliedConcepts;
    private Store store;
    private AnalysisListener reportWriter;
    private Logger logger;
    private ScriptEngineManager scriptEngineManager;
    private Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param ruleSet
     *            The rule set to execute.
     * @param executeAppliedConcepts
     *            If <code>true</code> then execute a concept even if it has already been applied.
     * @param store
     *            The store.
     * @param reportWriter
     *            The report writer.
     * @param log
     *            The logger.
     */
    public AnalyzerVisitor(RuleSet ruleSet, boolean executeAppliedConcepts, Store store, AnalysisListener reportWriter, Logger log) {
        this.ruleSet = ruleSet;
        this.executeAppliedConcepts = executeAppliedConcepts;
        this.store = store;
        this.reportWriter = reportWriter;
        this.logger = log;
        this.scriptEngineManager = new ScriptEngineManager();
        registerVerificationStrategy(new RowCountVerificationStrategy());
        registerVerificationStrategy(new AggregationVerificationStrategy());
    }

    private void registerVerificationStrategy(VerificationStrategy verificationStrategy) {
        verificationStrategies.put(verificationStrategy.getVerificationType(), verificationStrategy);
    }

    @Override
    public void visitConcept(Concept concept, Severity effectiveSeverity) throws AnalysisException {
        try {
            store.beginTransaction();
            ConceptDescriptor conceptDescriptor = store.find(ConceptDescriptor.class, concept.getId());
            if (conceptDescriptor == null || executeAppliedConcepts) {
                logger.info("Applying concept '" + concept.getId() + "' with severity: '" + concept.getSeverity().getInfo(effectiveSeverity)
                        + "'.");
                reportWriter.beginConcept(concept);
                reportWriter.setResult(execute(concept, ruleSet, effectiveSeverity));
                if (conceptDescriptor == null) {
                    conceptDescriptor = store.create(ConceptDescriptor.class);
                    conceptDescriptor.setId(concept.getId());
                }
                reportWriter.endConcept();
            }
            store.commitTransaction();
        } catch (XOException e) {
            store.rollbackTransaction();
            throw new AnalysisException("Cannot apply concept " + concept.getId(), e);
        }
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws AnalysisException {
        logger.info("Validating constraint '" + constraint.getId() + "' with severity: '" + constraint.getSeverity().getInfo(effectiveSeverity)
                + "'.");
        try {
            store.beginTransaction();
            reportWriter.beginConstraint(constraint);
            reportWriter.setResult(execute(constraint, ruleSet, effectiveSeverity));
            reportWriter.endConstraint();
            store.commitTransaction();
        } catch (XOException e) {
            store.rollbackTransaction();
            throw new AnalysisException("Cannot validate constraint " + constraint.getId(), e);
        }
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws AnalysisException {
        logger.info("Executing group '" + group.getId() + "'");
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
     * Run the given executableRule and return a result which can be passed to a report writer.
     *
     * @param executableRule
     *            The executableRule.
     * @param <T>
     *            The types of the executableRule.
     * @return The result.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If query execution fails.
     */
    private <T extends ExecutableRule> Result<T> execute(T executableRule, RuleSet ruleSet, Severity severity) throws AnalysisException {
        return execute(executableRule, executableRule.getExecutable(), ruleSet, severity);
    }

    private <T extends ExecutableRule> Result<T> execute(T executableRule, Executable executable, RuleSet ruleSet, Severity severity)
            throws AnalysisException {
        if (executable instanceof CypherExecutable) {
            return executeCypher(executableRule, (CypherExecutable) executable, severity);
        } else if (executable instanceof ScriptExecutable) {
            return executeScript(executableRule, (ScriptExecutable) executable, executableRule, severity);
        } else if (executable instanceof TemplateExecutable) {
            String templateId = ((TemplateExecutable) executable).getTemplateId();
            Template template;
            try {
                template = ruleSet.getTemplateBucket().getById(templateId);
            } catch (NoTemplateException e) {
                throw new AnalysisException("Unknown template with id " + templateId, e);
            }
            return execute(executableRule, template.getExecutable(), ruleSet, severity);
        } else {
            throw new AnalysisException("Unsupported executable type " + executable);
        }
    }

    /**
     * Execute the cypher query of a rule.
     * 
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
    private <T extends ExecutableRule> Result<T> executeCypher(T executableRule, CypherExecutable executable, Severity severity)
            throws AnalysisException {
        String cypher = executable.getStatement();
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Query.Result<Query.Result.CompositeRowObject> compositeRowObjects = executeQuery(cypher, executableRule.getParameters())) {
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
            Result.Status status = verify(executableRule, columnNames, rows);
            return new Result<>(executableRule, status, severity, columnNames, rows);
        } catch (Exception e) {
            throw new AnalysisException("Cannot execute query for rule '" + executableRule + "'.", e);
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
    private <T extends ExecutableRule> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows)
            throws AnalysisException {
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
     * @param scriptExecutable
     *            The script.
     * @param severity
     *            The severity.
     * @param <T>
     *            The result type.
     * @return The result.
     * @throws AnalysisException
     *             If script execution fails.
     */
    private <T extends ExecutableRule> Result<T> executeScript(T executableRule, ScriptExecutable scriptExecutable, T executable,
            Severity severity) throws AnalysisException {
        String language = scriptExecutable.getLanguage();
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
            scriptResult = scriptEngine.eval(scriptExecutable.getSource());
        } catch (ScriptException e) {
            throw new AnalysisException("Cannot execute script.", e);
        }
        if (!(scriptResult instanceof Result)) {
            throw new AnalysisException("Script returned an invalid result language, expected " + Result.class.getName() + " but got "
                    + scriptResult);
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
        logger.debug("Executing query '" + cypher + "' with parameters [" + parameters + "]");
        return store.executeQuery(cypher, parameters);
    }
}
