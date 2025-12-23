package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.rule.api.model.*;

/**
 * Abstract base implementation of a {@link RuleVisitor}.
 */
public abstract class AbstractRuleVisitor<R> implements RuleVisitor<R> {

    @Override
    public boolean isSuccess(R result) {
        return true;
    }

    @Override
    public void beforeRules(RuleSelection ruleSelection) throws RuleException {
    }

    @Override
    public void includedConcepts(List<Concept> concepts) {
    }

    @Override
    public void includedGroups(List<Group> groups) {
    }

    @Override
    public void includedConstraints(List<Constraint> constraints) {
    }

    @Override
    public void afterRules() throws RuleException {
    }

    @Override
    public void overriddenConcept(Concept concept, Concept overridingConcept) {
    }

    @Override
    public R visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults,
        Map<Concept, R> providingConceptResults) throws RuleException {
        throw new RuleException("Cannot visit concept" + concept);
    }

    @Override
    public void requiredConcepts(Concept concept, Set<Concept> requiredConcepts) {
    }

    @Override
    public void providingConcepts(Concept concept, Set<Concept> providingConcepts) {
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException {
    }

    @Override
    public void overriddenConstraint(Constraint constraint, Constraint overridingConstraint) {
    }

    @Override
    public R visitConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults)
        throws RuleException {
        throw new RuleException("Cannot visit constraint" + constraint);
    }

    @Override
    public void requiredConcepts(Constraint constraint, Set<Concept> concepts) {
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults)
        throws RuleException {
    }

    @Override
    public void overriddenGroup(Group group, Group overridingGroup) {
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
    }

    @Override
    public void includedConcepts(Group group, List<Concept> concepts) {
    }

    @Override
    public void includedGroups(Group group, List<Group> groups) {
    }

    @Override
    public void includedConstraints(Group group, List<Constraint> constraints) {
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
    }
}
