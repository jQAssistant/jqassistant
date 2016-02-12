package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.impl.RuleVisitor;

/**
 * Abstract base implementation of a {@link RuleVisitor}.
 */
public abstract class AbstractRuleVisitor implements RuleVisitor {

    @Override
    public void visitConcept(Concept concept, Severity effectiveSeverity) throws AnalysisException {
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws AnalysisException {
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws AnalysisException {
    }

    @Override
    public void afterGroup(Group group) throws AnalysisException {
    }
}
