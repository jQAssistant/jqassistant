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
    public void includedConcepts(List<Concept> includedConcepts) {
        delegate.includedConcepts(includedConcepts);
        this.includedConcepts(this.analyzeTaskDescriptor, includedConcepts);
    }

    @Override
    public void includedGroups(List<Group> groups) {
        delegate.includedGroups(groups);
        this.includedGroups(this.analyzeTaskDescriptor, groups);
    }

    @Override
    public void includedConstraints(List<Constraint> includedConstraints) {
        delegate.includedConstraints(includedConstraints);
        this.includedConstraints(this.analyzeTaskDescriptor, includedConstraints);
    }

    @Override
    public void overriddenConcept(Concept concept, Concept overridingConcept) {
        delegate.overriddenConcept(concept, overridingConcept);
        store.requireTransaction(() -> {
            ConceptDescriptor conceptDescriptor = getConceptDescriptor(concept);
            getConceptDescriptor(overridingConcept).setOverridesConcept(conceptDescriptor);
        });
    }

    @Override
    public Result.Status visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, Result.Status> requiredConceptResults,
        Map<Concept, Result.Status> providingConceptResults) throws RuleException {
        Result.Status status = delegate.visitConcept(concept, effectiveSeverity, requiredConceptResults, providingConceptResults);
        store.requireTransaction(() -> {
            ConceptDescriptor conceptDescriptor = getConceptDescriptor(concept);
            updateRule(conceptDescriptor, concept, effectiveSeverity);
            conceptDescriptor.setStatus(status);
        });
        return status;
    }

    @Override
    public void requiredConcepts(Concept concept, Set<Concept> requiredConcepts) {
        delegate.requiredConcepts(concept, requiredConcepts);
        requiredConcepts(getConceptDescriptor(concept), requiredConcepts);
    }

    @Override
    public void providingConcepts(Concept concept, Set<Concept> providingConcepts) {
        delegate.providingConcepts(concept, providingConcepts);
        store.requireTransaction(() -> {
            ConceptDescriptor conceptDescriptor = getConceptDescriptor(concept);
            for (Concept providingConcept : providingConcepts) {
                getConceptDescriptor(providingConcept).getProvidesConcepts()
                    .add(conceptDescriptor);
            }
        });
    }

    @Override
    public void overriddenGroup(Group group, Group overridingGroup) {
        delegate.overriddenGroup(group, overridingGroup);
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
    public void includedConcepts(Group group, List<Concept> includedConcepts) {
        delegate.includedConcepts(group, includedConcepts);
        this.includedConcepts(getGroupDescriptor(group), includedConcepts);
    }

    @Override
    public void includedGroups(Group group, List<Group> includedGroups) {
        delegate.includedGroups(group, includedGroups);
        this.includedGroups(getGroupDescriptor(group), includedGroups);
    }

    @Override
    public void includedConstraints(Group group, List<Constraint> includedConstraints) {
        delegate.includedConstraints(group, includedConstraints);
        this.includedConstraints(getGroupDescriptor(group), includedConstraints);
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
        delegate.afterGroup(group);
    }

    @Override
    public void overriddenConstraint(Constraint constraint, Constraint overridingConstraint) {
        delegate.overriddenConstraint(constraint, overridingConstraint);
        store.requireTransaction(() -> {
            ConstraintDescriptor constraintDescriptor = getConstraintDescriptor(constraint);
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
    public void requiredConcepts(Constraint constraint, Set<Concept> requiredConcepts) {
        delegate.requiredConcepts(constraint, requiredConcepts);
        requiredConcepts(getConstraintDescriptor(constraint), requiredConcepts);
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

    private void updateRule(RuleDescriptor ruleDescriptor, SeverityRule rule, Severity effectiveSeverity) {
        ruleDescriptor.setSeverity(rule.getSeverity());
        ruleDescriptor.setEffectiveSeverity(effectiveSeverity);
        ruleDescriptor.setTimestamp(now());
    }

    private void requiredConcepts(ExecutableRuleTemplate executableRuleTemplate, Set<Concept> requiredConcepts) {
        store.requireTransaction(() -> {
            for (Concept requiredConcept : requiredConcepts) {
                executableRuleTemplate.getRequiresConcepts()
                    .add(getConceptDescriptor(requiredConcept));
            }
        });
    }

    private void includedConcepts(RuleGroupTemplate ruleGroupTemplate, List<Concept> concepts) {
        store.requireTransaction(() -> {
            for (Concept concept : concepts) {
                ruleGroupTemplate.getIncludesConcepts()
                    .add(getConceptDescriptor(concept));
            }
        });
    }

    private void includedGroups(RuleGroupTemplate ruleGroupTemplate, List<Group> groups) {
        store.requireTransaction(() -> {
            for (Group group : groups) {
                ruleGroupTemplate.getIncludesGroups()
                    .add(getGroupDescriptor(group));
            }
        });
    }

    private void includedConstraints(RuleGroupTemplate ruleGroupTemplate, List<Constraint> constraints) {
        store.requireTransaction(() -> {
            for (Constraint constraint : constraints) {
                ruleGroupTemplate.getIncludesConstraints()
                    .add(getConstraintDescriptor(constraint));
            }
        });
    }
}
