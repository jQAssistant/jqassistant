package com.buschmais.jqassistant.core.rule.api.model;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.Getter;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;

/**
 * Defines a concept which can be applied.
 *
 * Used to add information to the database.
 */
@Getter
public class Concept extends AbstractExecutableRule {

    public static Severity DEFAULT_SEVERITY = MINOR;

    private final Set<String> providedConcepts = new LinkedHashSet<>();

    public static class ConceptBuilder extends AbstractExecutableRule.Builder<ConceptBuilder, Concept> {
        private ConceptBuilder(Concept rule) {
            super(rule);
        }

        @Override
        protected ConceptBuilder getThis() {
            return this;
        }

        public ConceptBuilder providedConcepts(Set<String> providedConcepts) {
            Concept r = build();
            r.providedConcepts.addAll(providedConcepts);
            return this;
        }
    }

    public static ConceptBuilder builder() {
        return new ConceptBuilder(new Concept());
    }
}
