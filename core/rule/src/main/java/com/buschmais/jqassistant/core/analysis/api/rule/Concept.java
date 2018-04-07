package com.buschmais.jqassistant.core.analysis.api.rule;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

/**
 * Defines a concept which can be applied.
 *
 * Used to add information to the database.
 */
public class Concept extends AbstractExecutableRule {

    protected Concept() {
    }

    public static Builder builder() {
        return new Builder(new Concept());
    }

    public static class Builder extends AbstractExecutableRule.Builder<Concept.Builder, Concept> {

        protected Builder(Concept rule) {
            super(rule);
        }

        @Deprecated
        @ToBeRemovedInVersion(major = 1, minor = 5)
        public static Builder newConcept() {
            return new Builder(new Concept());
        }
    }
}
