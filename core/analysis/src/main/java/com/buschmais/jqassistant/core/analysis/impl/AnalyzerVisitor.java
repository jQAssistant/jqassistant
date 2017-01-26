package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.rule.api.executor.AbstractRuleVisitor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleExecutorException;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
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

    private AnalyzerConfiguration configuration;
    private Map<String, String> ruleParameters;
    private Store store;
    private ReportPlugin reportPlugin;
    private Logger logger;
    private ScriptEngineManager scriptEngineManager;
    private Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param configuration
     *            THe configuration
     * @param ruleParameters
     * @param store
     *            The store.
     * @param reportPlugin
     *            The report writer.
     * @param log
     */
    public AnalyzerVisitor(AnalyzerConfiguration configuration, Map<String, String> ruleParameters, Store store, ReportPlugin reportPlugin, Logger log) {
        this.configuration = configuration;
        this.ruleParameters = ruleParameters;
        this.store = store;
        this.reportPlugin = reportPlugin;
        this.logger = log;
        this.scriptEngineManager = new ScriptEngineManager();
        registerVerificationStrategy(new RowCountVerificationStrategy());
        registerVerificationStrategy(new AggregationVerificationStrategy());
    }

    private void registerVerificationStrategy(VerificationStrategy verificationStrategy) {
        verificationStrategies.put(verificationStrategy.getVerificationType(), verificationStrategy);
    }

    @Override
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleExecutorException {
        try {
            store.beginTransaction();
            ConceptDescriptor conceptDescriptor = store.find(ConceptDescriptor.class, concept.getId());
            Result.Status status;
            if (conceptDescriptor == null || configuration.isExecuteAppliedConcepts()) {
                logger.info("Applying concept '" + concept.getId() + "' with severity: '" + concept.getSeverity().getInfo(effectiveSeverity) + "'.");
                reportPlugin.beginConcept(concept);
                Result<Concept> result = execute(concept, effectiveSeverity);
                reportPlugin.setResult(result);
                status = result.getStatus();
                if (conceptDescriptor == null) {
                    conceptDescriptor = store.create(ConceptDescriptor.class);
                    conceptDescriptor.setId(concept.getId());
                    conceptDescriptor.setStatus(result.getStatus());
                }
                reportPlugin.endConcept();
            } else {
                status = conceptDescriptor.getStatus();
            }
            store.commitTransaction();
            return Result.Status.SUCCESS.equals(status);
        } catch (XOException e) {
            store.rollbackTransaction();
            throw new RuleExecutorException("Cannot apply concept " + concept.getId(), e);
        }
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleExecutorException {
        store.beginTransaction();
        reportPlugin.beginConcept(concept);
        Result<Concept> result = new Result<>(concept, Result.Status.SKIPPED, effectiveSeverity, null, null);
        reportPlugin.setResult(result);
        reportPlugin.endConcept();
        store.commitTransaction();
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleExecutorException {
        logger.info("Validating constraint '" + constraint.getId() + "' with severity: '" + constraint.getSeverity().getInfo(effectiveSeverity) + "'.");
        try {
            store.beginTransaction();
            reportPlugin.beginConstraint(constraint);
            reportPlugin.setResult(execute(constraint, effectiveSeverity));
            reportPlugin.endConstraint();
            store.commitTransaction();
        } catch (XOException e) {
            store.rollbackTransaction();
            throw new RuleExecutorException("Cannot validate constraint " + constraint.getId(), e);
        }
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleExecutorException {
        store.beginTransaction();
        reportPlugin.beginConstraint(constraint);
        Result<Constraint> result = new Result<>(constraint, Result.Status.SKIPPED, effectiveSeverity, null, null);
        reportPlugin.setResult(result);
        reportPlugin.endConstraint();
        store.commitTransaction();
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleExecutorException {
        logger.info("Executing group '" + group.getId() + "'");
        store.beginTransaction();
        reportPlugin.beginGroup(group);
        store.commitTransaction();
    }

    @Override
    public void afterGroup(Group group) throws RuleExecutorException {
        store.beginTransaction();
        reportPlugin.endGroup();
        store.commitTransaction();
    }

    private <T extends ExecutableRule> Result<T> execute(T executableRule, Severity severity) throws RuleExecutorException {
        Map<String, Object> ruleParameters = getRuleParameters(executableRule);
        Executable executable = executableRule.getExecutable();
        // TODO extract to strategies
        if (executable instanceof CypherExecutable) {
            return executeCypher(executableRule, (CypherExecutable) executable, ruleParameters, severity);
        } else if (executable instanceof ScriptExecutable) {
            return executeScript(executableRule, (ScriptExecutable) executable, ruleParameters, severity);
        } else {
            throw new RuleExecutorException("Unsupported executable type " + executable);
        }
    }

    private Map<String, Object> getRuleParameters(ExecutableRule executableRule) throws RuleExecutorException {
        Map<String, Object> ruleParameters = new HashMap<>();
        Map<String, Parameter> parameters = executableRule.getParameters();
        for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            String parameterName = entry.getKey();
            Parameter parameter = entry.getValue();
            Object parameterValue;
            String parameterValueAsString = this.ruleParameters.get(parameterName);
            if (parameterValueAsString != null) {
                try {
                    parameterValue = parameter.getType().parse(parameterValueAsString);
                } catch (RuleException e) {
                    throw new RuleExecutorException("Cannot determine value for parameter " + parameterName + "' of rule '" + executableRule + "'.");
                }
            } else {
                parameterValue = parameter.getDefaultValue();
            }
            if (parameterValue == null) {
                throw new RuleExecutorException(
                        "No value or default value defined for required parameter '" + parameterName + "' of rule '" + executableRule + "'.");
            }
            ruleParameters.put(parameterName, parameterValue);
        }
        return ruleParameters;
    }

    /**
     * Execute the cypher query of a rule.
     * 
     * @param executableRule
     *            The executable.
     * @param executable
     *            The executable.
     * @param parameters
     *            The parameters.
     * @param severity
     *            The severity.
     * @param <T>
     *            The rule type.
     * @return The result.
     * @throws RuleExecutorException
     *             If execution fails.
     */
    private <T extends ExecutableRule> Result<T> executeCypher(T executableRule, CypherExecutable executable, Map<String, Object> parameters, Severity severity)
            throws RuleExecutorException {
        String cypher = executable.getStatement();
        List<Map<String, Object>> rows = new ArrayList<>();
        try (Query.Result<Query.Result.CompositeRowObject> compositeRowObjects = executeQuery(cypher, parameters)) {
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
            throw new RuleExecutorException("Cannot execute query for rule '" + executableRule + "'.", e);
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
     * @throws RuleExecutorException
     *             If no valid verification strategy can be found.
     */
    private <T extends ExecutableRule> Result.Status verify(T executable, List<String> columnNames, List<Map<String, Object>> rows) throws RuleExecutorException {
        Verification verification = executable.getVerification();
        VerificationStrategy strategy = verificationStrategies.get(verification.getClass());
        if (strategy == null) {
            throw new RuleExecutorException("Result verification not supported: " + verification.getClass().getName());
        }
        return strategy.verify(executable, verification, columnNames, rows);
    }

    /**
     * Execute an analysis script
     * 
     * @param <T>
     *            The result type.
     * @param scriptExecutable
     *            The script.
     * @param ruleParameters
     * @param severity
     *            The severity. @return The result.
     * @throws RuleExecutorException
     *             If script execution fails.
     */
    private <T extends ExecutableRule> Result<T> executeScript(T executable, ScriptExecutable scriptExecutable, Map<String, Object> ruleParameters,
            Severity severity) throws RuleExecutorException {
        String language = scriptExecutable.getLanguage();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByName(language);
        if (scriptEngine == null) {
            List<String> availableLanguages = new ArrayList<>();
            for (ScriptEngineFactory factory : scriptEngineManager.getEngineFactories()) {
                availableLanguages.addAll(factory.getNames());
            }
            throw new RuleExecutorException("Cannot resolve scripting engine for '" + language + "', available languages are " + availableLanguages);
        }
        // Set default variables
        scriptEngine.put(ScriptVariable.STORE.getVariableName(), store);
        scriptEngine.put(ScriptVariable.RULE.getVariableName(), executable);
        scriptEngine.put(ScriptVariable.SEVERITY.getVariableName(), severity);
        // Set rule parameters
        for (Map.Entry<String, Object> entry : ruleParameters.entrySet()) {
            scriptEngine.put(entry.getKey(), entry.getValue());
        }
        Object scriptResult;
        try {
            scriptResult = scriptEngine.eval(scriptExecutable.getSource());
        } catch (ScriptException e) {
            throw new RuleExecutorException("Cannot execute script.", e);
        }
        if (!(scriptResult instanceof Result)) {
            throw new RuleExecutorException("Script returned an invalid result type, expected " + Result.class.getName() + " but got " + scriptResult);
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
