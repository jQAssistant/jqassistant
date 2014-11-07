package com.buschmais.jqassistant.core.analysis.impl;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.rule.Concept;
import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;
import com.buschmais.jqassistant.core.analysis.api.rule.Group;
import com.buschmais.jqassistant.core.analysis.api.rule.Severity;

public interface RuleVisitor {

    void visitConcept(Concept concept, Severity severity) throws AnalysisException;

    void visitConstraint(Constraint constraint, Severity severity) throws AnalysisException;

    void beforeGroup(Group group) throws AnalysisException;

    void afterGroup(Group group) throws AnalysisException;

    boolean missingConcept(String id);

    boolean missingConstraint(String id);

    boolean missingGroup(String id);
}
