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
    public void includeConcepts(List<Concept> concepts) {
    }

    @Override
    public void includeGroups(List<Group> groups) {
    }

    @Override
    public void includeConstraints(List<Constraint> constraints) {
    }

    @Override
    public void afterRules() throws RuleException {
    }

    @Override
    public void overrideConcept(Concept concept, Concept overridingConcept) {
    }

    @Override
    public R visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults,
        Map<Concept, R> providingConceptResults) throws RuleException {
        throw new RuleException("Cannot visit concept" + concept);
    }

    @Override
    public void requireConcepts(Concept concept, Set<Concept> requiredConcepts) {
    }

    @Override
    public void provideConcept(Concept concept, Set<Concept> providingConcepts) {
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException {
    }

    @Override
    public void overrideConstraint(Constraint constraint, Constraint overridingConstraint) {
    }

    @Override
    public R visitConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults)
        throws RuleException {
        throw new RuleException("Cannot visit constraint" + constraint);
    }

    @Override
    public void requireConcepts(Constraint constraint, Set<Concept> concepts) {
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults)
        throws RuleException {
    }

    @Override
    public void overrideGroup(Group group, Group overridingGroup) {
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
    }

    @Override
    public void includeConcepts(Group group, List<Concept> concepts) {
    }

    @Override
    public void includeGroups(Group group, List<Group> groups) {
    }

    @Override
    public void includeConstraints(Group group, List<Constraint> constraints) {
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
    }
}
