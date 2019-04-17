package com.buschmais.jqassistant.core.rule.api.executor;

import com.buschmais.jqassistant.core.analysis.api.rule.*;

/**
 * Abstract base implementation of a {@link RuleVisitor}.
 */
public abstract class AbstractRuleVisitor implements RuleVisitor {

    @Override
    public void beforeRules() throws RuleException {
    }

    @Override
    public void afterRules() throws RuleException {
    }

    @Override
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        return true;
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
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
