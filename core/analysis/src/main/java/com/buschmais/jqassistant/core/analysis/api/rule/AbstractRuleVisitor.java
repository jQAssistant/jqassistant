package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.impl.RuleVisitor;

/**
 * Created by Dirk Mahler on 04.11.2014.
 */
public abstract class AbstractRuleVisitor implements RuleVisitor {

    @Override
    public void visitConcept(Concept concept, Severity severity) throws AnalysisException {
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity severity) throws AnalysisException {
    }

    @Override
    public void beforeGroup(Group group) throws AnalysisException {
    }

    @Override
    public void afterGroup(Group group) throws AnalysisException {
    }

    @Override
    public boolean missingConcept(String id) {
        return false;
    }

    @Override
    public boolean missingConstraint(String id) {
        return false;
    }

    @Override
    public boolean missingGroup(String id) {
        return false;
    }
}
