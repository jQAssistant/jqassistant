package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Map;
import java.util.Set;

import com.buschmais.jqassistant.core.analysis.api.rule.source.RuleSource;

/**
 * Defines a concept which can be applied.
 * <p>
 * Used to add information to the database.
 */
public class Concept extends AbstractExecutableRule {

    /**
     * Default severity level for concepts.
     */
    public static Severity DEFAULT_SEVERITY = Severity.MINOR;

    private Concept() {
    }

    public static class Builder extends AbstractExecutableRule.Builder<Concept.Builder, Concept> {

        protected Builder(Concept rule) {
            super(rule);
        }

        public static Builder newConcept() {
            return new Builder(new Concept());
        }
    }
}
