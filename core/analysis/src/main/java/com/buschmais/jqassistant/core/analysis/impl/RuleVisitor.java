package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

/**
 * Defines the visitor interface for executing rules.
 */
public interface RuleVisitor {

    void visitConcept(Concept concept, Severity effectiveSeverity) throws AnalysisException;

    void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws AnalysisException;

    void beforeGroup(Group group, Severity effectiveSeverity) throws AnalysisException;

    void afterGroup(Group group) throws AnalysisException;

}
