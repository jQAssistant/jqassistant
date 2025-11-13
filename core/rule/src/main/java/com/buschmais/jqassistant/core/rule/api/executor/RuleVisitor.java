package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.List;
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
    void beforeRules(RuleSelection ruleSelection) throws RuleException;

    void includedConcepts(List<Concept> includedConcepts);

    void includedGroups(List<Group> includedGroups);

    void includedConstraints(List<Constraint> includedConstraints);

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

    void requiredConcepts(Concept concept, List<Concept> requiredConcepts);

    void providingConcepts(Concept concept, List<Concept> providingConcepts);

    /**
     * Skip a concept.
     *
     * @param concept
     *     The concept.
     * @param effectiveSeverity
     *     The severity to use.
     * @param requiredConceptResults
     *     The results of required concepts.
     * @throws RuleException
     *     If an error occurred.
     */
    void skipConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException;

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

    void requiredConcepts(Constraint constraint, List<Concept> requiredConcepts);

    /**
     * Skip a constraint.
     *
     * @param constraint
     *     The constraint.
     * @param effectiveSeverity
     *     The severity to use.
     * @param requiredConceptResults
     *     The results of required concepts.
     * @throws RuleException
     *     If an error occurred.
     */
    void skipConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException;

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

    void includedConcepts(Group group, List<Concept> includedConcepts);

    void includedGroups(Group group, List<Group> includedGroups);

    void includedConstraints(Group group, List<Constraint> includedConstraints);

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
