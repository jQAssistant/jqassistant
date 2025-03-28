package com.buschmais.jqassistant.core.rule.api.model;

import java.util.*;

import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * A rule set builder.
 */
@Slf4j
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
        for (Map.Entry<String, Concept.Activation> providedConcept : concept.getProvidedConcepts()
            .entrySet()) {
            updateProvidedConcepts(providedConcept.getKey(), providingConceptId, providedConcept.getValue());
        }
        return this;
    }

    public RuleSetBuilder addConstraint(Constraint constraint) throws RuleException {
        ruleSet.constraintBucket.add(constraint);
        return this;
    }

    public RuleSetBuilder addGroup(Group group) throws RuleException {
        ruleSet.groupsBucket.add(group);
        for (Map.Entry<String, Map<String, Concept.Activation>> entry : group.getProvidedConcepts()
            .entrySet()) {
            String providedConceptId = entry.getKey();
            for (Map.Entry<String, Concept.Activation> providingConcept : entry.getValue()
                .entrySet()) {
                updateProvidedConcepts(providedConceptId, providingConcept.getKey(), providingConcept.getValue());
            }
        }
        return this;
    }

    private void updateProvidedConcepts(String providedConceptId, String providingConceptId, Concept.Activation activation) {
        ruleSet.providingConcepts.computeIfAbsent(providingConceptId, id -> new LinkedHashSet<>())
            .add(providedConceptId);
        ruleSet.providedConcepts.computeIfAbsent(providedConceptId, id -> new LinkedHashMap<>())
            .put(providingConceptId, activation);
    }

    public RuleSet getRuleSet() throws RuleException {
        validate();
        return ruleSet;
    }

    private void validate() throws RuleException {
        Set<String> conceptIds = ruleSet.getConceptBucket()
            .getIds();
        for (Map.Entry<String, Map<String, Concept.Activation>> entry : ruleSet.getProvidedConcepts()
            .entrySet()) {
            String providedConceptId = entry.getKey();
            if (!conceptIds.contains(providedConceptId)) {
                for (Map.Entry<String, Concept.Activation> providingConcept : entry.getValue()
                    .entrySet()) {
                    log.warn("Concept {} provides non-resolvable concept with id '{}'.", ruleSet.getConceptBucket()
                        .getById(providingConcept.getKey()), providedConceptId);
                }
            }
        }
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
        private final Map<String, Map<String, Concept.Activation>> providedConcepts = new HashMap<>();

        /**
         * Holds providing concepts as keys and their provided concepts as values
         */

        private DefaultRuleSet() {
        }
    }
}
