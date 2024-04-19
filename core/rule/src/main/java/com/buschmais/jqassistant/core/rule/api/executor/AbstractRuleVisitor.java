package com.buschmais.jqassistant.core.rule.api.executor;

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
    public void beforeRules() throws RuleException {
    }

    @Override
    public void afterRules() throws RuleException {
    }

    @Override
    public R visitConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        throw new RuleException("Cannot visit concept" + concept);
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
    }

    @Override
    public R visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        throw new RuleException("Cannot visit constraint" + constraint);
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
    }

    @Override
    public void afterGroup(Group group) throws RuleException{
    }
}
