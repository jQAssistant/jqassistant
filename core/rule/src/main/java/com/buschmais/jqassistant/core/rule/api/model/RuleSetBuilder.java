package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.ToString;

/**
 * A rule set builder.
 */
public class RuleSetBuilder {

    private final DefaultRuleSet ruleSet = new DefaultRuleSet();

    /**
     * Private constructor.
     */
    private RuleSetBuilder() {
    }

    public static RuleSetBuilder newInstance() {
        return new RuleSetBuilder();
    }

    public RuleSetBuilder addConcept(Concept concept) throws RuleException {
        ruleSet.conceptBucket.add(concept);
        String providingConceptId = concept.getId();
        for (String providedConceptId : concept.getProvidedConcepts()) {
            updateProvidedConcepts(providedConceptId, providingConceptId);
        }
        return this;
    }

    public RuleSetBuilder addConstraint(Constraint constraint) throws RuleException {
        ruleSet.constraintBucket.add(constraint);
        return this;
    }

    public RuleSetBuilder addGroup(Group group) throws RuleException {
        ruleSet.groupsBucket.add(group);
        for (Map.Entry<String, Set<String>> entry : group.getProvidedConcepts()
            .entrySet()) {
            String providedConceptId = entry.getKey();
            for (String providingConceptId : entry.getValue()) {
                updateProvidedConcepts(providedConceptId, providingConceptId);
            }
        }
        return this;
    }

    private void updateProvidedConcepts(String providedConceptId, String providingConceptId) {
        ruleSet.providingConcepts.computeIfAbsent(providingConceptId, id -> new LinkedHashSet<>())
            .add(providedConceptId);
        ruleSet.providedConcepts.computeIfAbsent(providedConceptId, id -> new LinkedHashSet<>())
            .add(providingConceptId);
    }

    public RuleSet getRuleSet() {
        return ruleSet;
    }

    /**
     * Defines a set of rules containing all resolved {@link Concept} s, {@link Constraint}s and {@link Group}s.
     */
    @Getter
    @ToString
    private static class DefaultRuleSet implements RuleSet {

        private final ConceptBucket conceptBucket = new ConceptBucket();
        private final Map<String, Set<String>> providingConcepts = new HashMap<>();
        private final ConstraintBucket constraintBucket = new ConstraintBucket();
        private final GroupsBucket groupsBucket = new GroupsBucket();

        /**
         * Holds provided concepts as keys and their providing concepts as values
         */
        private final Map<String, Set<String>> providedConcepts = new HashMap<>();

        /**
         * Holds providing concepts as keys and their provided concepts as values
         */

        private DefaultRuleSet() {
        }
    }
}
