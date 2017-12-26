package com.buschmais.jqassistant.core.analysis.impl;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerConfiguration;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.Result;
import com.buschmais.jqassistant.core.analysis.api.RuleExecutorPlugin;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.rule.*;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.rule.api.executor.AbstractRuleVisitor;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.xo.api.XOException;

import org.slf4j.Logger;

/**
 * Implementation of a rule visitor for analysis execution.
 */
public class AnalyzerVisitor extends AbstractRuleVisitor {

    private AnalyzerConfiguration configuration;
    private Map<String, String> ruleParameters;
    private ReportPlugin reportPlugin;
    private AnalyzerContext analyzerContext;
    private Map<Class<? extends Executable>, RuleExecutorPlugin<?>> ruleLanguagePlugins;

    /**
     * Constructor.
     *
     * @param configuration
     *            THe configuration
     * @param ruleParameters
     * @param store
     *            The context.getStore().
     * @param reportPlugin
     *            The report writer.
     * @param log
     */
    public AnalyzerVisitor(AnalyzerConfiguration configuration, Map<String, String> ruleParameters, Store store, ReportPlugin reportPlugin, Logger log) {
        this.configuration = configuration;
        this.ruleParameters = ruleParameters;
        this.reportPlugin = reportPlugin;
        this.ruleLanguagePlugins = initExecutorPlugins();
        this.analyzerContext = new AnalyzerContextImpl(store, log, initVerificationStrategies());
    }

    private Map<Class<? extends Executable>, RuleExecutorPlugin<?>> initExecutorPlugins() {
        Map<Class<? extends Executable>, RuleExecutorPlugin<?>> executorPluginMap = new HashMap<>();
        executorPluginMap.put(CypherExecutable.class, new CypherExecutorPlugin());
        executorPluginMap.put(ScriptExecutable.class, new ScriptExecutorPlugin());
        return executorPluginMap;
    }

    private Map<Class<? extends Verification>, VerificationStrategy> initVerificationStrategies() {
        Map<Class<? extends Verification>, VerificationStrategy> verificationStrategies = new HashMap<>();
        RowCountVerificationStrategy rowCountVerificationStrategy = new RowCountVerificationStrategy();
        verificationStrategies.put(rowCountVerificationStrategy.getVerificationType(), rowCountVerificationStrategy);
        AggregationVerificationStrategy aggregationVerificationStrategy = new AggregationVerificationStrategy();
        verificationStrategies.put(aggregationVerificationStrategy.getVerificationType(), aggregationVerificationStrategy);
        return verificationStrategies;
    }

    @Override
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        try {
            analyzerContext.getStore().beginTransaction();
            ConceptDescriptor conceptDescriptor = analyzerContext.getStore().find(ConceptDescriptor.class, concept.getId());
            Result.Status status;
            if (conceptDescriptor == null || configuration.isExecuteAppliedConcepts()) {
                analyzerContext.getLogger()
                        .info("Applying concept '" + concept.getId() + "' with severity: '" + concept.getSeverity().getInfo(effectiveSeverity) + "'.");
                reportPlugin.beginConcept(concept);
                Result<Concept> result = execute(concept, effectiveSeverity);
                reportPlugin.setResult(result);
                status = result.getStatus();
                if (conceptDescriptor == null) {
                    conceptDescriptor = analyzerContext.getStore().create(ConceptDescriptor.class);
                    conceptDescriptor.setId(concept.getId());
                    conceptDescriptor.setStatus(result.getStatus());
                }
                reportPlugin.endConcept();
            } else {
                status = conceptDescriptor.getStatus();
            }
            analyzerContext.getStore().commitTransaction();
            return Result.Status.SUCCESS.equals(status);
        } catch (XOException e) {
            analyzerContext.getStore().rollbackTransaction();
            throw new RuleException("Cannot apply concept " + concept.getId(), e);
        }
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        analyzerContext.getStore().beginTransaction();
        reportPlugin.beginConcept(concept);
        Result<Concept> result = new Result<>(concept, Result.Status.SKIPPED, effectiveSeverity, null, null);
        reportPlugin.setResult(result);
        reportPlugin.endConcept();
        analyzerContext.getStore().commitTransaction();
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        analyzerContext.getLogger()
                .info("Validating constraint '" + constraint.getId() + "' with severity: '" + constraint.getSeverity().getInfo(effectiveSeverity) + "'.");
        try {
            analyzerContext.getStore().beginTransaction();
            reportPlugin.beginConstraint(constraint);
            reportPlugin.setResult(execute(constraint, effectiveSeverity));
            reportPlugin.endConstraint();
            analyzerContext.getStore().commitTransaction();
        } catch (XOException e) {
            analyzerContext.getStore().rollbackTransaction();
            throw new RuleException("Cannot validate constraint " + constraint.getId(), e);
        }
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        analyzerContext.getStore().beginTransaction();
        reportPlugin.beginConstraint(constraint);
        Result<Constraint> result = new Result<>(constraint, Result.Status.SKIPPED, effectiveSeverity, null, null);
        reportPlugin.setResult(result);
        reportPlugin.endConstraint();
        analyzerContext.getStore().commitTransaction();
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
        analyzerContext.getLogger().info("Executing group '" + group.getId() + "'");
        analyzerContext.getStore().beginTransaction();
        reportPlugin.beginGroup(group);
        analyzerContext.getStore().commitTransaction();
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
        analyzerContext.getStore().beginTransaction();
        reportPlugin.endGroup();
        analyzerContext.getStore().commitTransaction();
    }

    private <T extends ExecutableRule, E extends Executable> Result<T> execute(T executableRule, Severity severity) throws RuleException {
        Map<String, Object> ruleParameters = getRuleParameters(executableRule);
        E executable = (E) executableRule.getExecutable();
        RuleExecutorPlugin<E> ruleLanguagePlugin = (RuleExecutorPlugin<E>) ruleLanguagePlugins.get(executable.getClass());
        if (ruleLanguagePlugin != null) {
            return ruleLanguagePlugin.execute(executableRule, ruleParameters, severity, analyzerContext);
        } else {
            throw new RuleException("Unsupported executable type " + executable);
        }
    }

    private Map<String, Object> getRuleParameters(ExecutableRule executableRule) throws RuleException {
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
                } catch (com.buschmais.jqassistant.core.analysis.api.rule.RuleException e) {
                    throw new RuleException("Cannot determine value for parameter " + parameterName + "' of rule '" + executableRule + "'.");
                }
            } else {
                parameterValue = parameter.getDefaultValue();
            }
            if (parameterValue == null) {
                throw new RuleException(
                        "No value or default value defined for required parameter '" + parameterName + "' of rule '" + executableRule + "'.");
            }
            ruleParameters.put(parameterName, parameterValue);
        }
        return ruleParameters;
    }

}
