package com.buschmais.jqassistant.core.analysis.impl;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.api.model.*;
import com.buschmais.jqassistant.core.analysis.spi.RuleRepository;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.executor.AbstractRuleVisitor;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.store.api.Store;

import io.smallrye.config.ConfigMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import static com.buschmais.jqassistant.core.analysis.api.configuration.Analyze.EXECUTE_APPLIED_CONCEPTS;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.FAILURE;
import static com.buschmais.jqassistant.core.report.api.model.Result.Status.SUCCESS;
import static java.time.LocalDateTime.now;
import static java.util.Collections.*;

/**
 * Implementation of a rule visitor for analysis execution.
 */
@Slf4j
public class AnalyzerRuleVisitor extends AbstractRuleVisitor<Result.Status> {

    private final Analyze configuration;
    private final AnalyzerContext analyzerContext;
    private final ReportPlugin reportPlugin;
    private final Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins;
    private final Store store;
    private final RuleRepository ruleRepository;
    private final Deque<RuleGroupTemplate> ruleGroups = new ArrayDeque<>();

    /**
     * Constructor.
     *
     * @param configuration
     *     The configuration
     * @param analyzerContext
     *     The {@link AnalyzerContext}
     * @param ruleInterpreterPlugins
     *     The {@link RuleInterpreterPlugin}s.
     * @param reportPlugin
     *     The report writer.
     */
    AnalyzerRuleVisitor(Analyze configuration, AnalyzerContext analyzerContext, Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins,
        ReportPlugin reportPlugin) {
        this.configuration = configuration;
        this.analyzerContext = analyzerContext;
        this.ruleInterpreterPlugins = ruleInterpreterPlugins;
        this.reportPlugin = reportPlugin;
        this.store = analyzerContext.getStore();
        this.ruleRepository = store.getXOManager()
            .getRepository(RuleRepository.class);
    }

    @Override
    public boolean isSuccess(Result.Status result) {
        return SUCCESS.equals(result);
    }

    @Override
    public void beforeRules() throws RuleException {
        store.requireTransaction(() -> {
            AnalyzeTaskDescriptor analyzeTaskDescriptor = store.create(AnalyzeTaskDescriptor.class);
            analyzeTaskDescriptor.setTimestamp(now());
            ruleGroups.push(analyzeTaskDescriptor);
            reportPlugin.begin();
        });
    }

    @Override
    public void afterRules() throws RuleException {
        store.requireTransaction(reportPlugin::end);
        ruleGroups.pop();
    }

    @Override
    public Result.Status visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults,
        Map<Concept, Result.Status> providingConceptResults) throws RuleException {
        ConceptDescriptor conceptDescriptor = findConcept(concept);
        if (conceptDescriptor == null || configuration.executeAppliedConcepts()) {
            log.info("Applying concept '{}' with severity: '{}'.", concept.getId(), effectiveSeverity.getInfo(concept.getSeverity()));
            store.requireTransaction(() -> reportPlugin.beginConcept(concept, requiredConceptResults, providingConceptResults));
            Result<Concept> result = execute(concept, effectiveSeverity);
            store.requireTransaction(() -> reportPlugin.setResult(result));
            store.requireTransaction(reportPlugin::endConcept);
            Result.Status status = evaluateConceptStatus(result, providingConceptResults);
            updateConcept(concept, effectiveSeverity, providingConceptResults.keySet(), status);
            return status;
        } else {
            log.info("Concept '{}' has already been applied, skipping (activate '{}.{}' to force execution).", concept.getId(),
                Analyze.class.getAnnotation(ConfigMapping.class)
                    .prefix(), EXECUTE_APPLIED_CONCEPTS);
            return store.requireTransaction(conceptDescriptor::getStatus);
        }
    }

    private static Result.Status evaluateConceptStatus(Result<Concept> result, Map<Concept, Result.Status> providedConceptResults) {
        return Stream.of(singletonList(result.getStatus()), providedConceptResults.values())
            .flatMap(Collection::stream)
            .filter(SUCCESS::equals)
            .findAny()
            .orElse(FAILURE);
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults)
        throws RuleException {
        store.requireTransaction(() -> reportPlugin.beginConcept(concept, requiredConceptResults, emptyMap()));
        Result<Concept> result = Result.<Concept>builder()
            .rule(concept)
            .status(Result.Status.SKIPPED)
            .severity(effectiveSeverity)
            .build();
        store.requireTransaction(() -> reportPlugin.setResult(result));
        store.requireTransaction(reportPlugin::endConcept);
    }

    @Override
    public Result.Status visitConstraint(Constraint constraint, Severity effectiveSeverity,
        Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults) throws RuleException {
        log.info("Validating constraint '{}' with severity: '{}'.", constraint.getId(), effectiveSeverity.getInfo(constraint.getSeverity()));
        store.requireTransaction(() -> reportPlugin.beginConstraint(constraint, requiredConceptResults));
        Result<Constraint> result = execute(constraint, effectiveSeverity);
        store.requireTransaction(() -> reportPlugin.setResult(result));
        store.requireTransaction(reportPlugin::endConstraint);
        Result.Status status = result.getStatus();
        updateConstraint(constraint, effectiveSeverity, status);
        return status;
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults)
        throws RuleException {
        store.requireTransaction(() -> reportPlugin.beginConstraint(constraint, requiredConceptResults));
        Result<Constraint> result = Result.<Constraint>builder()
            .rule(constraint)
            .status(Result.Status.SKIPPED)
            .severity(effectiveSeverity)
            .build();
        store.requireTransaction(() -> reportPlugin.setResult(result));
        store.requireTransaction(reportPlugin::endConstraint);
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
        log.info("Executing group '{}'", group.getId());
        store.requireTransaction(() -> reportPlugin.beginGroup(group));
        updateGroup(group, effectiveSeverity);

    }

    @Override
    public void afterGroup(Group group) throws RuleException {
        store.requireTransaction(reportPlugin::endGroup);
        ruleGroups.pop();
    }

    private <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Severity severity) throws RuleException {
        Executable<?> executable = executableRule.getExecutable();
        if (executable == null) {
            return Result.<T>builder()
                .rule(executableRule)
                .status(SUCCESS)
                .severity(severity)
                .columnNames(emptyList())
                .rows(emptyList())
                .build();
        } else {
            Map<String, Object> ruleParameters = getRuleParameters(executableRule);
            Collection<RuleInterpreterPlugin> languagePlugins = ruleInterpreterPlugins.get(executable.getLanguage());
            if (languagePlugins == null) {
                throw new RuleException("Could not determine plugin to execute " + executableRule);
            }
            for (RuleInterpreterPlugin languagePlugin : languagePlugins) {
                if (languagePlugin.accepts(executableRule)) {
                    Result<T> result = execute(executableRule, severity, ruleParameters, languagePlugin);
                    if (result != null) {
                        return result;
                    }
                }
            }
            throw new RuleException("No plugin for language '" + executable.getLanguage() + "' returned a result for " + executableRule);
        }
    }

    private <T extends ExecutableRule<?>> Result<T> execute(T executableRule, Severity severity, Map<String, Object> ruleParameters,
        RuleInterpreterPlugin languagePlugin) throws RuleException {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            return languagePlugin.execute(executableRule, ruleParameters, severity, analyzerContext);
        } finally {
            stopWatch.stop();
            long ruleExecutionTime = stopWatch.getTime(TimeUnit.SECONDS);
            if (ruleExecutionTime > configuration.warnOnExecutionTimeSeconds()) {
                log.warn("Execution of rule with id '{}' took {} seconds.", executableRule.getSource()
                    .getId(), ruleExecutionTime);
            }
            if (store.hasActiveTransaction()) {
                log.warn("Rule with id '{}' returned with an active transaction, performing rollback. Please check the implementation.",
                    executableRule.getSource()
                        .getId());
                store.rollbackTransaction();
            }
        }
    }

    private Map<String, Object> getRuleParameters(ExecutableRule<?> executableRule) throws RuleException {
        Map<String, Object> ruleParameters = new HashMap<>();
        Map<String, Parameter> parameters = executableRule.getParameters();
        for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            String parameterName = entry.getKey();
            Parameter parameter = entry.getValue();
            Object parameterValue;
            String parameterValueAsString = configuration.ruleParameters()
                .get(parameterName);
            if (parameterValueAsString != null) {
                try {
                    parameterValue = parameter.getType()
                        .parse(parameterValueAsString);
                } catch (RuleException e) {
                    throw new RuleException("Cannot determine value for parameter " + parameterName + "' of rule '" + executableRule + "'.");
                }
            } else {
                parameterValue = parameter.getDefaultValue();
            }
            if (parameterValue == null) {
                throw new RuleException("No value or default value defined for required parameter '" + parameterName + "' of rule '" + executableRule + "'.");
            }
            ruleParameters.put(parameterName, parameterValue);
        }
        return ruleParameters;
    }

    private ConceptDescriptor findConcept(Concept concept) {
        return store.requireTransaction(() -> this.ruleRepository.findConcept(concept.getId()));
    }

    private void updateConcept(Concept concept, Severity effectiveSeverity, Set<Concept> providingConcepts, Result.Status status) {
        store.requireTransaction(() -> {
            ConceptDescriptor conceptDescriptor = this.ruleRepository.mergeConcept(concept.getId());
            updateRule(concept, effectiveSeverity, conceptDescriptor);
            updateExecutableRule(concept, status, conceptDescriptor);
            for (Concept providingConcept : providingConcepts) {
                this.ruleRepository.mergeConcept(providingConcept.getId())
                    .getProvidesConcepts()
                    .add(conceptDescriptor);
            }
            updateRuleGroup(ruleGroup -> ruleGroup.getIncludesConcepts()
                .add(conceptDescriptor));
        });
    }

    private void updateConstraint(Constraint constraint, Severity effectiveSeverity, Result.Status status) {
        store.requireTransaction(() -> {
            ConstraintDescriptor constraintDescriptor = this.ruleRepository.mergeConstraint(constraint.getId());
            updateRule(constraint, effectiveSeverity, constraintDescriptor);
            updateExecutableRule(constraint, status, constraintDescriptor);
            updateRuleGroup(ruleGroup -> ruleGroup.getIncludesConstraints()
                .add(constraintDescriptor));
        });
    }

    private void updateGroup(Group group, Severity effectiveSeverity) {
        store.requireTransaction(() -> {
            GroupDescriptor groupDescriptor = this.ruleRepository.mergeGroup(group.getId());
            updateRule(group, effectiveSeverity, groupDescriptor);
            updateRuleGroup(ruleGroup -> ruleGroup.getIncludesGroups()
                .add(groupDescriptor));
            ruleGroups.push(groupDescriptor);
        });
    }

    private void updateRuleGroup(Consumer<RuleGroupTemplate> ruleGroupConsumer) {
        if (!ruleGroups.isEmpty()) {
            ruleGroupConsumer.accept(ruleGroups.peek());
        }
    }

    private void updateRule(SeverityRule rule, Severity effectiveSeverity, RuleDescriptor ruleDescriptor) {
        ruleDescriptor.setSeverity(rule.getSeverity());
        ruleDescriptor.setEffectiveSeverity(effectiveSeverity);
    }

    private void updateExecutableRule(ExecutableRule<?> executableRule, Result.Status status, ExecutableRuleTemplate executableRuleTemplate) {
        for (String requiresConceptId : executableRule.getRequiresConcepts()
            .keySet()) {
            executableRuleTemplate.getRequiresConcepts()
                .add(this.ruleRepository.mergeConcept(requiresConceptId));
        }
        executableRuleTemplate.setStatus(status);
    }
}
