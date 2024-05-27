package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.Map;

import com.buschmais.jqassistant.core.rule.api.model.*;

/**
 * Defines the visitor interface for executing rules.
 */
public interface RuleVisitor<R> {

    /**
     * Determines if the given result is considered a successful execution of a rule and further dependent rules can be executed.
     *
     * @param result
     *     The result.
     * @return <code>true</code> If the result represents a successful execution.
     */
    boolean isSuccess(R result);

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
     *     The concept.
     * @param effectiveSeverity
     *     The severity to use.
     * @param requiredConceptResults
     *     The results of required concepts.
     * @param providingConceptResults
     *     The results of providing concepts.
     * @return The result of the visitor.
     * @throws RuleException
     *     If an error occurred.
     */
    R visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults,
        Map<Concept, R> providingConceptResults) throws RuleException;

    /**
     * Skip a concept.
     *
     * @param concept
     *     The concept.
     * @param effectiveSeverity
     *     The severity to use.
     * @throws RuleException
     *     If an error occurred.
     */
    void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException;

    /**
     * Visit a constraint with the given severity.
     *
     * @param constraint
     *     The constraint.
     * @param effectiveSeverity
     *     The severity to use.
     * @param requiredConceptResults
     *     The results of required concepts.
     * @return The result of the visitor.
     * @throws RuleException
     *     If an error occurred.
     */
    R visitConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException;

    /**
     * Skip a constraint.
     *
     * @param constraint
     *     The constraint.
     * @param effectiveSeverity
     *     The severity to use.
     * @throws RuleException
     *     If an error occurred.
     */
    void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException;

    /**
     * Start processing a group.
     *
     * @param group
     *     The group.
     * @param effectiveSeverity
     *     The severity.
     * @throws RuleException
     *     If an error occurred.
     */
    void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException;

    /**
     * Finish processing a group.
     *
     * @param group
     *     The group.
     * @throws RuleException
     *     If an error occurred.
     */
    void afterGroup(Group group) throws RuleException;
}
