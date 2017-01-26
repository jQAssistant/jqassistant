package com.buschmais.jqassistant.core.rule.api.executor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.*;

/**
 * Executes rules.
 */
public class RuleExecutor {

    private Map<Concept, Boolean> executedConcepts = new HashMap<>();

    private Set<Constraint> executedConstraints = new HashSet<>();

    private Set<Group> executedGroups = new HashSet<>();

    private RuleVisitor ruleVisitor;

    private RuleExecutorConfiguration configuration;

    public RuleExecutor(RuleVisitor ruleVisitor, RuleExecutorConfiguration configuration) {
        this.ruleVisitor = ruleVisitor;
        this.configuration = configuration;
    }

    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws RuleExecutorException {
        for (String conceptId : ruleSelection.getConceptIds()) {
            Concept concept = resolveConcept(ruleSet, conceptId);
            applyConcept(ruleSet, concept, concept.getSeverity());
        }
        for (String constraintId : ruleSelection.getConstraintIds()) {
            Constraint constraint = resolveConstraint(ruleSet, constraintId);
            validateConstraint(ruleSet, constraint, constraint.getSeverity());
        }
        for (String groupId : ruleSelection.getGroupIds()) {
            Group group = resolveGroup(ruleSet, groupId);
            executeGroup(ruleSet, group, group.getSeverity());
        }
    }

    /**
     * Executes the given group.
     *
     * @param ruleSet
     *            The rule set.
     * @param group
     *            The group.
     * @param severity
     *            The severity.
     */
    private void executeGroup(RuleSet ruleSet, Group group, Severity severity) throws RuleExecutorException {
        if (!executedGroups.contains(group)) {
            ruleVisitor.beforeGroup(group, getEffectiveSeverity(group, severity, severity));
            for (Map.Entry<String, Severity> groupEntry : group.getGroups().entrySet()) {
                String groupId = groupEntry.getKey();
                Group includedGroup = resolveGroup(ruleSet, groupId);
                executeGroup(ruleSet, includedGroup, getEffectiveSeverity(includedGroup, severity, groupEntry.getValue()));
            }
            Map<String, Severity> concepts = group.getConcepts();
            for (Map.Entry<String, Severity> conceptEntry : concepts.entrySet()) {
                String conceptId = conceptEntry.getKey();
                Concept concept = resolveConcept(ruleSet, conceptId);
                applyConcept(ruleSet, concept, getEffectiveSeverity(concept, severity, conceptEntry.getValue()));
            }
            Map<String, Severity> constraints = group.getConstraints();
            for (Map.Entry<String, Severity> constraintEntry : constraints.entrySet()) {
                String constraintId = constraintEntry.getKey();
                Constraint constraint = resolveConstraint(ruleSet, constraintId);
                validateConstraint(ruleSet, constraint, getEffectiveSeverity(constraint, severity, constraintEntry.getValue()));
            }
            executedGroups.add(group);
            ruleVisitor.afterGroup(group);
        }
    }

    /**
     * Determines the effective severity for a rule to be executed.
     *
     * @param rule
     *            The rule.
     * @param parentSeverity
     *            The severity inherited from the parent group.
     * @param ruleSeverity
     *            The severity as specified on the rule in the parent group.
     * @return The effective severity.
     */
    private Severity getEffectiveSeverity(SeverityRule rule, Severity parentSeverity, Severity ruleSeverity) {
        Severity managedSeverity = ruleSeverity != null ? ruleSeverity : parentSeverity;
        return managedSeverity != null ? managedSeverity : rule.getSeverity();
    }

    /**
     * Validates the given constraint.
     *
     * @param constraint
     *            The constraint.
     * @throws RuleExecutorException
     *             If the constraint cannot be validated.
     */
    private void validateConstraint(RuleSet ruleSet, Constraint constraint, Severity severity) throws RuleExecutorException {
        if (!executedConstraints.contains(constraint)) {
            if (applyRequiredConcepts(ruleSet, constraint)) {
                ruleVisitor.visitConstraint(constraint, severity);
            } else {
                ruleVisitor.skipConstraint(constraint, severity);
            }
            executedConstraints.add(constraint);
        }
    }

    private boolean applyRequiredConcepts(RuleSet ruleSet, ExecutableRule rule) throws RuleExecutorException {
        boolean requiredConceptsApplied = true;
        for (Map.Entry<String, Boolean> entry : rule.getRequiresConcepts().entrySet()) {
            String conceptId = entry.getKey();
            Concept requiredConcept = resolveConcept(ruleSet, conceptId);
            boolean conceptResult = applyConcept(ruleSet, requiredConcept, requiredConcept.getSeverity());
            Boolean optional = entry.getValue();
            if (optional == null) {
                optional = configuration.isRequiredConceptsAreOptionalByDefault();
            }
            requiredConceptsApplied = requiredConceptsApplied && (conceptResult || optional);
        }
        return requiredConceptsApplied;
    }

    /**
     * Applies the given concept.
     *
     * @param concept
     *            The concept.
     * @throws RuleExecutorException
     *             If the concept cannot be applied.
     */
    private boolean applyConcept(RuleSet ruleSet, Concept concept, Severity severity) throws RuleExecutorException {
        Boolean result =  executedConcepts.get(concept);
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

    public Concept resolveConcept(RuleSet ruleSet, String requiredConceptId) throws RuleExecutorException {
        try {
            return ruleSet.getConceptBucket().getById(requiredConceptId);
        } catch (NoConceptException e) {
            throw new RuleExecutorException("Concept '" + requiredConceptId + "' is not defined.");
        }
    }

    public Constraint resolveConstraint(RuleSet ruleSet, String constraintId) throws RuleExecutorException {
        try {
            return ruleSet.getConstraintBucket().getById(constraintId);
        } catch (NoRuleException e) {
            throw new RuleExecutorException("Constraint '" + constraintId + "' not found.");
        }
    }

    public Group resolveGroup(RuleSet ruleSet, String groupId) throws RuleExecutorException {
        try {
            return ruleSet.getGroupsBucket().getById(groupId);
        } catch (NoGroupException e) {
            throw new RuleExecutorException("Group '" + groupId + "' is not defined.", e);
        }
    }

}
