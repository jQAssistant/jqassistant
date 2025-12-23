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
     * Start processing {@link Group}s, {@link Concept}s and {@link Constraint}s using a {@link RuleSelection}.
     */
    void beforeRules(RuleSelection ruleSelection) throws RuleException;

    /**
     * Include {@link Concept}s in the rule selection.
     *
     * @param includedConcepts
     *     The included {@link Concept}s.
     */
    void includedConcepts(List<Concept> includedConcepts);

    /**
     * Include {@link Group}s in the rule selection.
     *
     * @param includedGroups
     *     The included {@link Group}s.
     */
    void includedGroups(List<Group> includedGroups);

    /**
     * Include {@link Constraint}s in the rule selection.
     *
     * @param includedConstraints
     *     The included {@link Constraint}s.
     */
    void includedConstraints(List<Constraint> includedConstraints);

    /**
     * Finished processing {@link Group}s, {@link Concept}s and {@link Constraint}s.
     */
    void afterRules() throws RuleException;

    /**
     * Override a {@link Concept}.
     *
     * @param concept
     *     The overridden {@link Concept}.
     * @param overridingConcept
     *     The {@link Concept} that overrides the {@link Concept}.
     */
    void overriddenConcept(Concept concept, Concept overridingConcept);

    /**
     * Visit a {@link Concept} with the given severity.
     *
     * @param concept
     *     The {@link Concept}.
     * @param effectiveSeverity
     *     The {@link Severity} to use.
     * @param requiredConceptResults
     *     The results of required {@link Concept}s.
     * @param providingConceptResults
     *     The results of providing {@link Concept}s.
     * @return The result of the visitor.
     * @throws RuleException
     *     If an error occurred.
     */
    R visitConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults,
        Map<Concept, R> providingConceptResults) throws RuleException;

    /**
     * Require {@link Concept}s for a given {@link Concept}.
     *
     * @param concept
     *     The {@link Concept}.
     * @param requiredConcepts
     *     The required {@link Concept}s.
     */
    void requiredConcepts(Concept concept, List<Concept> requiredConcepts);

    /**
     * Provide {@link Concept}s for a given {@link Concept}.
     *
     * @param concept
     *     The provided {@link Concept}.
     * @param providingConcepts
     *     The providing {@link Concept}s.
     */
    void providingConcepts(Concept concept, List<Concept> providingConcepts);

    /**
     * Skip a {@link Concept}.
     *
     * @param concept
     *     The {@link Concept}.
     * @param effectiveSeverity
     *     The {@link Severity} to use.
     * @param requiredConceptResults
     *     The results of required {@link Concept}s.
     * @throws RuleException
     *     If an error occurred.
     */
    void skipConcept(Concept concept, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException;

    /**
     * Override a {@link Constraint}.
     *
     * @param constraint
     *     The overridden {@link Constraint}.
     * @param overridingConstraint
     *     The {@link Constraint} that overrides the {@link Constraint}.
     */
    void overriddenConstraint(Constraint constraint, Constraint overridingConstraint);

    /**
     * Visit a {@link Constraint} with the given {@link Severity}.
     *
     * @param constraint
     *     The {@link Constraint}.
     * @param effectiveSeverity
     *     The {@link Severity} to use.
     * @param requiredConceptResults
     *     The results of required {@link Concept}s.
     * @return The result of the visitor.
     * @throws RuleException
     *     If an error occurred.
     */
    R visitConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException;

    /**
     * Require {@link Concept}s for a given {@link Constraint}.
     *
     * @param constraint
     *     The {@link Constraint}.
     * @param requiredConcepts
     *     The required {@link Concept}s.
     */
    void requiredConcepts(Constraint constraint, List<Concept> requiredConcepts);

    /**
     * Skip a {@link Constraint}.
     *
     * @param constraint
     *     The {@link Constraint}.
     * @param effectiveSeverity
     *     The {@link Severity} to use.
     * @param requiredConceptResults
     *     The results of required {@link Concept}s.
     * @throws RuleException
     *     If an error occurred.
     */
    void skipConstraint(Constraint constraint, Severity effectiveSeverity, Map<Map.Entry<Concept, Boolean>, R> requiredConceptResults) throws RuleException;

    /**
     * Override a {@link Group}.
     *
     * @param group
     *     The overridden {@link Group}.
     * @param overridingGroup
     *     The {@link Group} that overrides the {@link Group}.
     */
    void overriddenGroup(Group group, Group overridingGroup);

    /**
     * Start processing a {@link Group}.
     *
     * @param group
     *     The {@link Group}.
     * @param effectiveSeverity
     *     The {@link Severity}.
     * @throws RuleException
     *     If an error occurred.
     */
    void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException;

    /**
     * The included {@link Concept}s of a {@link Group}.
     *
     * @param group
     *     The {@link Group}.
     * @param includedConcepts
     *     The included {@link Concept}s.
     */
    void includedConcepts(Group group, List<Concept> includedConcepts);

    /**
     * The included {@link Group}s of a {@link Group}.
     *
     * @param group
     *     The {@link Group}.
     * @param includedGroups
     *     The included {@link Group}s.
     */
    void includedGroups(Group group, List<Group> includedGroups);

    /**
     * The included {@link Constraint}s of a {@link Group}.
     *
     * @param group
     *     The {@link Group}.
     * @param includedConstraints
     *     The included {@link Constraint}s.
     */
    void includedConstraints(Group group, List<Constraint> includedConstraints);

    /**
     * Finish processing a group.
     *
     * @param group
     *     The {@link Group}.
     * @throws RuleException
     *     If an error occurred.
     */
    void afterGroup(Group group) throws RuleException;
}
