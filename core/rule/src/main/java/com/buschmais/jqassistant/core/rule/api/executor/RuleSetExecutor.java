package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.*;

import com.buschmais.jqassistant.core.analysis.api.rule.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controls execution of {@link RuleSet}s.
 * <p/>
 * Execution order is determined by
 * <ul>
 * <li>Declared dependencies between rules, i.e. required concepts</li>
 * <li>Group hierarchy: first concepts of a group, second all nested groups and
 * finally all constraints of a group.</li>
 * </ul>
 * The second rule ensures that before any constraint of a group is validated
 * all concepts and nested concepts are applied. This allows for non-explicit
 * dependencies between rules, e.g. a constraint relying on a specific label may
 * include results of concepts that are not explicitly required.
 */
public class RuleSetExecutor {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleSetExecutor.class);

    private Map<Concept, Boolean> executedConcepts = new HashMap<>();

    private Set<Constraint> executedConstraints = new HashSet<>();

    private Set<Group> executedGroups = new HashSet<>();

    private RuleVisitor ruleVisitor;

    private RuleSetExecutorConfiguration configuration;

    public RuleSetExecutor(RuleVisitor ruleVisitor, RuleSetExecutorConfiguration configuration) {
        this.ruleVisitor = ruleVisitor;
        this.configuration = configuration;
    }

    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws RuleException {
        this.ruleVisitor.beforeRules();
        try {
            for (String conceptPattern : ruleSelection.getConceptIds()) {
                applyConcepts(ruleSet, conceptPattern, null, null);
            }
            for (String groupPattern : ruleSelection.getGroupIds()) {
                executeGroups(ruleSet, groupPattern, null, null);
            }
            for (String constraintPattern : ruleSelection.getConstraintIds()) {
                validateConstraints(ruleSet, constraintPattern, null, null);
            }
        } finally {
            this.ruleVisitor.afterRules();
        }
    }

    /**
     * Executes the given group.
     *
     * @param ruleSet
     *            The rule set.
     * @param group
     *            The group.
     * @param parentSeverity
     *            The severity.
     */
    private void executeGroup(RuleSet ruleSet, Group group, Severity parentSeverity) throws RuleException {
        if (!executedGroups.contains(group)) {
            ruleVisitor.beforeGroup(group, getEffectiveSeverity(group, parentSeverity, parentSeverity));
            for (Map.Entry<String, Severity> conceptEntry : group.getConcepts().entrySet()) {
                applyConcepts(ruleSet, conceptEntry.getKey(), parentSeverity, conceptEntry.getValue());
            }
            for (Map.Entry<String, Severity> groupEntry : group.getGroups().entrySet()) {
                executeGroups(ruleSet, groupEntry.getKey(), parentSeverity, groupEntry.getValue());
            }
            Map<String, Severity> constraints = group.getConstraints();
            for (Map.Entry<String, Severity> constraintEntry : constraints.entrySet()) {
                validateConstraints(ruleSet, constraintEntry.getKey(), parentSeverity, constraintEntry.getValue());
            }
            ruleVisitor.afterGroup(group);
            executedGroups.add(group);
        }
    }

    private void applyConcepts(RuleSet ruleSet, String conceptPattern, Severity parentSeverity, Severity requestedSeverity) throws RuleException {
        List<Concept> matchingConcepts = ruleSet.getConceptBucket().match(conceptPattern);
        if (matchingConcepts.isEmpty()) {
            LOGGER.warn("Could not find concepts matching to '{}'.", matchingConcepts);
        } else {
            for (Concept matchingConcept : matchingConcepts) {
                applyConcept(ruleSet, matchingConcept, getEffectiveSeverity(matchingConcept, parentSeverity, requestedSeverity));
            }
        }
    }

    private void executeGroups(RuleSet ruleSet, String groupPattern, Severity parentSeverity, Severity requestedSeverity) throws RuleException {
        List<Group> matchingGroups = ruleSet.getGroupsBucket().match(groupPattern);
        if (matchingGroups.isEmpty()) {
            LOGGER.warn("Could not find groups matching to '{}'.", groupPattern);
        } else {
            for (Group matchingGroup : matchingGroups) {
                executeGroup(ruleSet, matchingGroup, getEffectiveSeverity(matchingGroup, parentSeverity, requestedSeverity));
            }
        }
    }

    private void validateConstraints(RuleSet ruleSet, String constraintPattern, Severity parentSeverity, Severity requestedSeverity) throws RuleException {
        List<Constraint> matchingConstraints = ruleSet.getConstraintBucket().match(constraintPattern);
        if (matchingConstraints.isEmpty()) {
            LOGGER.warn("Could not find constraints matching to '{}'.", constraintPattern);
        } else {
            for (Constraint matchingConstraint : matchingConstraints) {
                validateConstraint(ruleSet, matchingConstraint, getEffectiveSeverity(matchingConstraint, parentSeverity, requestedSeverity));
            }
        }
    }

    /**
     * Determines the effective severity for a rule to be executed.
     *
     * @param rule
     *            The rule.
     * @param parentSeverity
     *            The severity inherited from the parent group.
     * @param requestedSeverity
     *            The severity as specified on the rule in the parent group.
     * @return The effective severity.
     */
    private Severity getEffectiveSeverity(SeverityRule rule, Severity parentSeverity, Severity requestedSeverity) {
        Severity effectiveSeverity = requestedSeverity != null ? requestedSeverity : parentSeverity;
        return effectiveSeverity != null ? effectiveSeverity : rule.getSeverity();
    }

    /**
     * Validates the given constraint.
     *
     * @param constraint
     *            The constraint.
     * @throws RuleException
     *             If the constraint cannot be validated.
     */
    private void validateConstraint(RuleSet ruleSet, Constraint constraint, Severity severity) throws RuleException {
        if (!executedConstraints.contains(constraint)) {
            if (applyRequiredConcepts(ruleSet, constraint)) {
                ruleVisitor.visitConstraint(constraint, severity);
            } else {
                ruleVisitor.skipConstraint(constraint, severity);
            }
            executedConstraints.add(constraint);
        }
    }

    private boolean applyRequiredConcepts(RuleSet ruleSet, ExecutableRule<?> rule) throws RuleException {
        boolean requiredConceptsApplied = true;
        for (Map.Entry<String, Boolean> entry : rule.getRequiresConcepts().entrySet()) {
            List<Concept> requiredConcepts = ruleSet.getConceptBucket().match(entry.getKey());
            for (Concept requiredConcept : requiredConcepts) {
                boolean conceptResult = applyConcept(ruleSet, requiredConcept, requiredConcept.getSeverity());
                Boolean optional = entry.getValue();
                if (optional == null) {
                    optional = configuration.isRequiredConceptsAreOptionalByDefault();
                }
                requiredConceptsApplied = requiredConceptsApplied && (conceptResult || optional);
            }
        }
        return requiredConceptsApplied;
    }

    /**
     * Applies the given concept.
     *
     * @param concept
     *            The concept.
     * @throws RuleException
     *             If the concept cannot be applied.
     */
    private boolean applyConcept(RuleSet ruleSet, Concept concept, Severity severity) throws RuleException {
        Boolean result = executedConcepts.get(concept);
        if (result == null) {
            if (applyRequiredConcepts(ruleSet, concept)) {
                result = ruleVisitor.visitConcept(concept, severity);
            } else {
                ruleVisitor.skipConcept(concept, severity);
                result = false;
            }
            executedConcepts.put(concept, result);
        }
        return result;
    }
}
