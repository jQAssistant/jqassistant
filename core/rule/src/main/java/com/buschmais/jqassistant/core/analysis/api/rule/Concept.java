package com.buschmais.jqassistant.core.analysis.api.rule;

/**
 * Defines a concept which can be applied.
 *
 * Used to add information to the database.
 */
public class Concept extends AbstractExecutableRule {

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
