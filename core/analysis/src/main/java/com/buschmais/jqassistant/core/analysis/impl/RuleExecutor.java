package com.buschmais.jqassistant.core.analysis.impl;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.AnalysisException;
import com.buschmais.jqassistant.core.analysis.api.RuleSelection;
import com.buschmais.jqassistant.core.analysis.api.rule.*;

/**
 * Implementation of the
 * {@link com.buschmais.jqassistant.core.analysis.api.Analyzer}.
 */
public class RuleExecutor {

    private Set<Concept> executedConcepts = new HashSet<>();

    private Set<Constraint> executedConstraints = new HashSet<>();

    private Set<Group> executedGroups = new HashSet<>();

    private RuleVisitor ruleVisitor;

    public RuleExecutor(RuleVisitor ruleVisitor) {
        this.ruleVisitor = ruleVisitor;
    }

    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws AnalysisException {
        for (String conceptId : ruleSelection.getConceptIds()) {
            Concept concept = resolveConcept(ruleSet, conceptId);
            applyConcept(ruleSet, concept, concept.getSeverity());
        }
        for (String constraintId : ruleSelection.getConstraintIds()) {
            Constraint constraint = resolveConstraint(ruleSet, constraintId);
            validateConstraint(ruleSet, constraint, constraint.getSeverity());
        }
        for (String groupId : ruleSelection.getGroupIds()) {
            executeGroup(ruleSet, resolveGroup(ruleSet, groupId));
        }
    }

    /**
     * Executes the given group.
     * 
     * @param group
     *            The group.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the group cannot be executed.
     */
    private void executeGroup(RuleSet ruleSet, Group group) throws AnalysisException {
        if (!executedGroups.contains(group)) {
            for (String includedGroupId : group.getGroups()) {
                Group includedGroup = resolveGroup(ruleSet, includedGroupId);
                executeGroup(ruleSet, includedGroup);
            }
            ruleVisitor.beforeGroup(group);

            Map<String, Severity> concepts = group.getConcepts();
            for (Map.Entry<String, Severity> conceptEntry : concepts.entrySet()) {
                String conceptId = conceptEntry.getKey();
                Severity severity = conceptEntry.getValue();
                Concept concept = resolveConcept(ruleSet, conceptId);
                applyConcept(ruleSet, concept, severity);
            }
            for (Map.Entry<String, Severity> constraintEntry : group.getConstraints().entrySet()) {
                String constraintId = constraintEntry.getKey();
                Severity severity = constraintEntry.getValue();
                Constraint constraint = resolveConstraint(ruleSet, constraintId);
                validateConstraint(ruleSet, constraint, severity);
            }

            executedGroups.add(group);
            ruleVisitor.afterGroup(group);
        }
    }

    /**
     * Validates the given constraint.
     * 
     * @param constraint
     *            The constraint.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the constraint cannot be validated.
     */
    private void validateConstraint(RuleSet ruleSet, Constraint constraint, Severity severity) throws AnalysisException {
        if (!executedConstraints.contains(constraint)) {
            for (String requiredConceptId : constraint.getRequiresConcepts()) {
                Concept requiredConcept = resolveConcept(ruleSet, requiredConceptId);
                applyConcept(ruleSet, requiredConcept, requiredConcept.getSeverity());
            }
            ruleVisitor.visitConstraint(constraint, severity);
            executedConstraints.add(constraint);
        }
    }

    /**
     * Applies the given concept.
     * 
     * @param concept
     *            The concept.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisListenerException
     *             If the report cannot be written.
     * @throws com.buschmais.jqassistant.core.analysis.api.AnalysisException
     *             If the concept cannot be applied.
     */
    private void applyConcept(RuleSet ruleSet, Concept concept, Severity severity) throws AnalysisException {
        if (!executedConcepts.contains(concept)) {
            for (String requiredConceptId : concept.getRequiresConcepts()) {
                Concept requiredConcept = resolveConcept(ruleSet, requiredConceptId);
                applyConcept(ruleSet, requiredConcept, requiredConcept.getSeverity());
            }
            ruleVisitor.visitConcept(concept, severity);
            executedConcepts.add(concept);
        }
    }

    public Template resolveQueryTemplate(RuleSet ruleSet, String queryTemplateId) throws AnalysisException {
        Template template = ruleSet.getQueryTemplates().get(queryTemplateId);
        if (template == null) {
            throw new AnalysisException("Query template '" + queryTemplateId + " is not defined.");
        }
        return template;
    }

    public Concept resolveConcept(RuleSet ruleSet, String requiredConceptId) throws AnalysisException {
        Concept requiredConcept = ruleSet.getConcepts().get(requiredConceptId);
        if (requiredConcept == null) {
            throw new AnalysisException("Concept '" + requiredConceptId + "' is not defined.");
        }
        return requiredConcept;
    }

    public Constraint resolveConstraint(RuleSet ruleSet, String constraintId) throws AnalysisException {
        Constraint constraint = ruleSet.getConstraints().get(constraintId);
        if (constraint == null) {
            throw new AnalysisException("Constraint '" + constraintId + "' not found.");
        }
        return constraint;
    }

    public Group resolveGroup(RuleSet ruleSet, String groupId) throws AnalysisException {
        Group group = ruleSet.getGroups().get(groupId);
        if (group == null) {
            throw new AnalysisException("Group '" + groupId + "' is not defined.");
        }
        return group;
    }

}
