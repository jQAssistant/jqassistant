package com.buschmais.jqassistant.core.analysis.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.model.*;
import com.buschmais.jqassistant.core.analysis.spi.RuleRepository;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;
import com.buschmais.jqassistant.core.rule.api.model.*;
import com.buschmais.jqassistant.core.store.api.Store;

import lombok.experimental.Delegate;

import static java.time.ZonedDateTime.now;

public class AnalyzerRuleVisitorAuditDecorator implements RuleVisitor<Result.Status> {

    @Delegate
    private final AnalyzerRuleVisitor delegate;

    private final Store store;

    private final RuleRepository ruleRepository;

    private AnalyzeTaskDescriptor analyzeTaskDescriptor;

    private final Map<String, GroupDescriptor> groups = new HashMap<>();

    private final Map<String, ConceptDescriptor> concepts = new HashMap<>();

    private final Map<String, ConstraintDescriptor> constraints = new HashMap<>();

    AnalyzerRuleVisitorAuditDecorator(AnalyzerRuleVisitor delegate, Store store) {
        this.delegate = delegate;
        this.store = store;
        this.ruleRepository = store.getXOManager()
            .getRepository(RuleRepository.class);
    }

    @Override
    public void beforeRules(RuleSelection ruleSelection) throws RuleException {
        delegate.beforeRules(ruleSelection);
        store.requireTransaction(() -> {
            this.analyzeTaskDescriptor = store.create(AnalyzeTaskDescriptor.class);
            this.analyzeTaskDescriptor.setTimestamp(now());
        });
    }

    @Override
    public void includeConcepts(List<Concept> includedConcepts) {
        delegate.includeConcepts(includedConcepts);
        this.includeConcepts(this.analyzeTaskDescriptor, includedConcepts);
    }

    @Override
    public void includeGroups(List<Group> groups) {
        delegate.includeGroups(groups);
        this.includeGroups(this.analyzeTaskDescriptor, groups);
    }

    @Override
    public void includeConstraints(List<Constraint> includedConstraints) {
        delegate.includeConstraints(includedConstraints);
        this.includeConstraints(this.analyzeTaskDescriptor, includedConstraints);
    }

    @Override
    public void overrideConcept(Concept concept, Concept overridingConcept, Severity effectiveSeverity) {
        delegate.overrideConcept(concept, overridingConcept, effectiveSeverity);
        store.requireTransaction(() -> {
            ConceptDescriptor conceptDescriptor = getConceptDescriptor(concept);
            updateRule(conceptDescriptor, concept, effectiveSeverity, Result.Status.SKIPPED);
            getConceptDescriptor(overridingConcept).setOverridesConcept(conceptDescriptor);
        });
    }

    @Override
    public Result.Status visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults,
        Map<Concept, Result.Status> providingConceptResults) throws RuleException {
        Result.Status status = delegate.visitConcept(concept, effectiveSeverity, requiredConceptResults, providingConceptResults);
        store.requireTransaction(() -> {
            ConceptDescriptor conceptDescriptor = getConceptDescriptor(concept);
            updateRule(conceptDescriptor, concept, effectiveSeverity, status);
        });
        return status;
    }

    @Override
    public void requireConcepts(Concept concept, Map<Concept, Result.Status> requiredConcepts) {
        delegate.requireConcepts(concept, requiredConcepts);
        requireConcepts(getConceptDescriptor(concept), requiredConcepts.keySet());
    }

    @Override
    public void provideConcept(Concept concept, Map<Concept, Result.Status> providingConcepts) {
        delegate.provideConcept(concept, providingConcepts);
        store.requireTransaction(() -> {
            ConceptDescriptor conceptDescriptor = getConceptDescriptor(concept);
            for (Concept providingConcept : providingConcepts.keySet()) {
                getConceptDescriptor(providingConcept).getProvidesConcepts()
                    .add(conceptDescriptor);
            }
        });
    }

    @Override
    public void overrideGroup(Group group, Group overridingGroup, Severity overriddenSeverity) {
        delegate.overrideGroup(group, overridingGroup, overriddenSeverity);
        store.requireTransaction(() -> {
            GroupDescriptor groupDescriptor = getGroupDescriptor(group);
            getGroupDescriptor(overridingGroup).setOverridesGroup(groupDescriptor);
        });
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
        delegate.beforeGroup(group, effectiveSeverity);
        store.requireTransaction(() -> updateRule(getGroupDescriptor(group), group, effectiveSeverity));
    }

    @Override
    public void includeConcepts(Group group, List<Concept> includedConcepts) {
        delegate.includeConcepts(group, includedConcepts);
        this.includeConcepts(getGroupDescriptor(group), includedConcepts);
    }

    @Override
    public void includeGroups(Group group, List<Group> includedGroups) {
        delegate.includeGroups(group, includedGroups);
        this.includeGroups(getGroupDescriptor(group), includedGroups);
    }

    @Override
    public void includeConstraints(Group group, List<Constraint> includedConstraints) {
        delegate.includeConstraints(group, includedConstraints);
        this.includeConstraints(getGroupDescriptor(group), includedConstraints);
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
        delegate.afterGroup(group);
    }

    @Override
    public void overrideConstraint(Constraint constraint, Constraint overridingConstraint, Severity effectiveSeverity) {
        delegate.overrideConstraint(constraint, overridingConstraint, effectiveSeverity);
        store.requireTransaction(() -> {
            ConstraintDescriptor constraintDescriptor = getConstraintDescriptor(constraint);
            updateRule(constraintDescriptor, constraint, effectiveSeverity, Result.Status.SKIPPED);
            getConstraintDescriptor(overridingConstraint).setOverridesConstraint(constraintDescriptor);
        });
    }

    @Override
    public Result.Status visitConstraint(Constraint constraint, Severity effectiveSeverity,
        Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults) throws RuleException {
        Result.Status status = delegate.visitConstraint(constraint, effectiveSeverity, requiredConceptResults);
        store.requireTransaction(() -> {
            ConstraintDescriptor constraintDescriptor = this.ruleRepository.mergeConstraint(constraint.getId());
            updateRule(constraintDescriptor, constraint, effectiveSeverity);
            constraintDescriptor.setStatus(status);
        });
        return status;
    }

    @Override
    public void requireConcepts(Constraint constraint, Map<Concept, Result.Status> requiredConcepts) {
        delegate.requireConcepts(constraint, requiredConcepts);
        requireConcepts(getConstraintDescriptor(constraint), requiredConcepts.keySet());
    }

    private ConceptDescriptor getConceptDescriptor(Concept concept) {
        return this.concepts.computeIfAbsent(concept.getId(), this.ruleRepository::mergeConcept);
    }

    private ConstraintDescriptor getConstraintDescriptor(Constraint constraint) {
        return this.constraints.computeIfAbsent(constraint.getId(), this.ruleRepository::mergeConstraint);
    }

    private GroupDescriptor getGroupDescriptor(Group group) {
        return this.groups.computeIfAbsent(group.getId(), this.ruleRepository::mergeGroup);
    }

    private <D extends RuleDescriptor & ExecutableRuleTemplate> void updateRule(D ruleDescriptor, SeverityRule rule, Severity effectiveSeverity,
        Result.Status status) {
        updateRule(ruleDescriptor, rule, effectiveSeverity);
        ruleDescriptor.setStatus(status);
    }

    private void updateRule(RuleDescriptor ruleDescriptor, SeverityRule rule, Severity effectiveSeverity) {
        ruleDescriptor.setSeverity(rule.getSeverity());
        ruleDescriptor.setEffectiveSeverity(effectiveSeverity);
        ruleDescriptor.setTimestamp(now());
    }

    private void requireConcepts(ExecutableRuleTemplate executableRuleTemplate, Set<Concept> requiredConcepts) {
        store.requireTransaction(() -> {
            for (Concept requiredConcept : requiredConcepts) {
                executableRuleTemplate.getRequiresConcepts()
                    .add(getConceptDescriptor(requiredConcept));
            }
        });
    }

    private void includeConcepts(RuleGroupTemplate ruleGroupTemplate, List<Concept> concepts) {
        store.requireTransaction(() -> {
            for (Concept concept : concepts) {
                ruleGroupTemplate.getIncludesConcepts()
                    .add(getConceptDescriptor(concept));
            }
        });
    }

    private void includeGroups(RuleGroupTemplate ruleGroupTemplate, List<Group> groups) {
        store.requireTransaction(() -> {
            for (Group group : groups) {
                ruleGroupTemplate.getIncludesGroups()
                    .add(getGroupDescriptor(group));
            }
        });
    }

    private void includeConstraints(RuleGroupTemplate ruleGroupTemplate, List<Constraint> constraints) {
        store.requireTransaction(() -> {
            for (Constraint constraint : constraints) {
                ruleGroupTemplate.getIncludesConstraints()
                    .add(getConstraintDescriptor(constraint));
            }
        });
    }
}
