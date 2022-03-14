package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Singular;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;

/**
 * Defines a concept which can be applied.
 *
 * Used to add information to the database.
 */
@Getter
public class Concept extends AbstractExecutableRule {

    public static Severity DEFAULT_SEVERITY = MINOR;

    @Singular
    private Set<String> providesConcepts = new HashSet<>();

    public static class ConceptBuilder extends AbstractExecutableRule.Builder<ConceptBuilder, Concept> {
        private ConceptBuilder(Concept rule) {
            super(rule);
        }

        @Override
        protected ConceptBuilder getThis() {
            return this;
        }

        public ConceptBuilder providesConcepts(Set<String> requiresConcepts) {
            Concept r = build();
            r.providesConcepts.addAll(requiresConcepts);
            return this;
        }
    }

    public static ConceptBuilder builder() {
        return new ConceptBuilder(new Concept());
    }
}
