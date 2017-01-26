package com.buschmais.jqassistant.core.rule.api.executor;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

/**
 * Abstract base implementation of a {@link RuleVisitor}.
 */
public abstract class AbstractRuleVisitor implements RuleVisitor {

    @Override
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleExecutorException {
        return true;
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleExecutorException {
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleExecutorException {
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleExecutorException {
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleExecutorException {
    }

    @Override
    public void afterGroup(Group group) throws RuleExecutorException {
    }
}
