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

    /**
     * Visit a concept with the given severity.
     * 
     * @param concept
     *            The concept.
     * @param effectiveSeverity
     *            The severity to use.
     * @return <code>true</code> if the concept could be applied.
     * @throws AnalysisException
     *             If an error occurred.
     */
    boolean visitConcept(Concept concept, Severity effectiveSeverity) throws AnalysisException;

    /**
     * Skip a concept.
     * 
     * @param concept
     *            The concept.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws AnalysisException
     *             If an error occurred.
     */
    void skipConcept(Concept concept, Severity effectiveSeverity) throws AnalysisException;

    /**
     * Visit a constraint with the given severity.
     *
     * @param constraint
     *            The constraint.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws AnalysisException
     *             If an error occurred.
     */
    void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws AnalysisException;

    /**
     * Skip a constraint.
     *
     * @param constraint
     *            The constraint.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws AnalysisException
     *             If an error occurred.
     */
    void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws AnalysisException;

    /**
     * Start processing a group.
     * 
     * @param group
     *            The group.
     * @param effectiveSeverity
     *            The severity.
     * @throws AnalysisException
     *             If an error occurred.
     */
    void beforeGroup(Group group, Severity effectiveSeverity) throws AnalysisException;

    /**
     * Finish processing a group.
     * 
     * @param group
     *            The group.
     * @throws AnalysisException
     *             If an error occurred.
     */
    void afterGroup(Group group) throws AnalysisException;

}
