package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashSet;
import java.util.Set;

import lombok.Getter;
import lombok.Singular;

/**
 * Defines a concept which can be applied.
 *
 * Used to add information to the database.
 */
@Getter
public class Concept extends AbstractExecutableRule {

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
    }

    public static ConceptBuilder builder() {
        return new ConceptBuilder(new Concept());
    }
}
