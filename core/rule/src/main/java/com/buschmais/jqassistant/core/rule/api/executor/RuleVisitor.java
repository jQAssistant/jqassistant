package com.buschmais.jqassistant.core.rule.api.executor;

import com.buschmais.jqassistant.core.rule.api.model.Concept;
import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.rule.api.model.Group;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.Severity;

/**
 * Defines the visitor interface for executing rules.
 */
public interface RuleVisitor {

    /**
     * Start processing groups, concepts and constraints.
     */
    void beforeRules() throws RuleException;

    /**
     * Finished processing groups, concepts and constraints.
     */
    void afterRules() throws RuleException;

    /**
     * Visit a concept with the given severity.
     *
     * @param concept
     *            The concept.
     * @param effectiveSeverity
     *            The severity to use.
     * @return <code>true</code> if the concept could be applied.
     * @throws RuleException
     *             If an error occurred.
     */
    boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleException;

    /**
     * Skip a concept.
     *
     * @param concept
     *            The concept.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws RuleException
     *             If an error occurred.
     */
    void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException;

    /**
     * Visit a constraint with the given severity.
     *
     * @param constraint
     *            The constraint.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws RuleException
     *             If an error occurred.
     */
    void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException;

    /**
     * Skip a constraint.
     *
     * @param constraint
     *            The constraint.
     * @param effectiveSeverity
     *            The severity to use.
     * @throws RuleException
     *             If an error occurred.
     */
    void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException;

    /**
     * Start processing a group.
     *
     * @param group
     *            The group.
     * @param effectiveSeverity
     *            The severity.
     * @throws RuleException
     *             If an error occurred.
     */
    void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException;

    /**
     * Finish processing a group.
     *
     * @param group
     *            The group.
     * @throws RuleException
     *             If an error occurred.
     */
    void afterGroup(Group group) throws RuleException;

}
