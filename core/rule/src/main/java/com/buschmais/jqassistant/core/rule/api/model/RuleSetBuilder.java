package com.buschmais.jqassistant.core.rule.api.model;

import java.util.*;

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
        for (String providesConceptId : concept.getProvidedConcepts()) {
            ruleSet.providedConcepts.computeIfAbsent(providesConceptId, id -> new LinkedHashSet<>())
                .add(providingConceptId);
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
            ruleSet.providedConcepts.computeIfAbsent(entry.getKey(), id -> new LinkedHashSet<>())
                .addAll(entry.getValue());
        }
        return this;
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
        private final Map<String, Set<String>> providedConcepts = new HashMap<>();
        private final ConstraintBucket constraintBucket = new ConstraintBucket();
        private final GroupsBucket groupsBucket = new GroupsBucket();

        private DefaultRuleSet() {
        }
    }
}
