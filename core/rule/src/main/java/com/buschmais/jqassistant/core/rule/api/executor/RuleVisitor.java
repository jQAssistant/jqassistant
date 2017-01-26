package com.buschmais.jqassistant.core.rule.api.executor;

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
     * @throws RuleExecutorException
     *             If an error occurred.
     */
    boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleExecutorException;

    /**
     * Skip a concept.
     * 
     * @param concept
     *            The concept.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws RuleExecutorException
     *             If an error occurred.
     */
    void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleExecutorException;

    /**
     * Visit a constraint with the given severity.
     *
     * @param constraint
     *            The constraint.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws RuleExecutorException
     *             If an error occurred.
     */
    void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleExecutorException;

    /**
     * Skip a constraint.
     *
     * @param constraint
     *            The constraint.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws RuleExecutorException
     *             If an error occurred.
     */
    void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleExecutorException;

    /**
     * Start processing a group.
     * 
     * @param group
     *            The group.
     * @param effectiveSeverity
     *            The severity.
     * @throws RuleExecutorException
     *             If an error occurred.
     */
    void beforeGroup(Group group, Severity effectiveSeverity) throws RuleExecutorException;

    /**
     * Finish processing a group.
     * 
     * @param group
     *            The group.
     * @throws RuleExecutorException
     *             If an error occurred.
     */
    void afterGroup(Group group) throws RuleExecutorException;

}
