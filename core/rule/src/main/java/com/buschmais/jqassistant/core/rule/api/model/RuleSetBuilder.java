package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
        ruleSet.conceptBucket.updateOverrideConcepts(concept);
        updateProvidedConcepts(concept.getProvidedConcepts());
        return this;
    }

    public RuleSetBuilder addConstraint(Constraint constraint) throws RuleException {
        ruleSet.constraintBucket.add(constraint);
        return this;
    }

    public RuleSetBuilder addGroup(Group group) throws RuleException {
        ruleSet.groupsBucket.add(group);
        for (Map.Entry<String, Set<Concept.ProvidedConcept>> entry : group.getProvidedConcepts()
            .entrySet()) {
            updateProvidedConcepts(entry.getValue());
        }
        return this;
    }

    private void updateProvidedConcepts(Set<Concept.ProvidedConcept> providedConcepts) {
        for (Concept.ProvidedConcept providedConcept : providedConcepts) {
            ruleSet.providingConceptIds.computeIfAbsent(providedConcept.getProvidingConceptId(), id -> new LinkedHashSet<>())
                .add(providedConcept.getProvidedConceptId());
            ruleSet.providedConcepts.computeIfAbsent(providedConcept.getProvidedConceptId(), id -> new LinkedHashSet<>())
                .add(providedConcept);
        }
    }

    public RuleSet getRuleSet() throws RuleException {
        validate();
        return ruleSet;
    }

    private void validate() throws RuleException {
        Set<String> conceptIds = ruleSet.getConceptBucket()
            .getIds();
        for (Map.Entry<String, Set<Concept.ProvidedConcept>> entry : ruleSet.getProvidedConcepts()
            .entrySet()) {
            String providedConceptId = entry.getKey();
            if (!conceptIds.contains(providedConceptId)) {
                for (Concept.ProvidedConcept providedConcept : entry.getValue()) {
                    log.warn("Concept {} provides non-resolvable concept with id '{}'.", ruleSet.getConceptBucket()
                        .getById(providedConcept.getProvidingConceptId()), providedConceptId);
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
        private final Map<String, Set<String>> providingConceptIds = new HashMap<>();
        private final ConstraintBucket constraintBucket = new ConstraintBucket();
        private final GroupsBucket groupsBucket = new GroupsBucket();

        /**
         * Holds provided concepts as keys and their providing concepts as values
         */
        private final Map<String, Set<Concept.ProvidedConcept>> providedConcepts = new HashMap<>();

        /**
         * Holds providing concepts as keys and their provided concepts as values
         */

        private DefaultRuleSet() {
        }
    }
}
