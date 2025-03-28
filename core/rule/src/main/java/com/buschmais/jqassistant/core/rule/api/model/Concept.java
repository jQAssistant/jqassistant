package com.buschmais.jqassistant.core.rule.api.model;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Getter;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;

/**
 * Defines a concept which can be applied.
 * <p>
 * Used to add information to the database.
 */
@Getter
public class Concept extends AbstractExecutableRule {

    public static Severity DEFAULT_SEVERITY = MINOR;

    /**
     * Determines the activation policy of providing concepts
     */
    public enum Activation {
        /**
         * Activate the providing concept if it is available.
         */
        IF_AVAILABLE,
        /**
         * Activate the providing concept only if it is explicitly or transitively required by the user.
         */
        IF_REQUIRED;
    }

    private final Map<String, Activation> providedConcepts = new LinkedHashMap<>();

    public static class ConceptBuilder extends AbstractExecutableRule.Builder<ConceptBuilder, Concept> {
        private ConceptBuilder(Concept rule) {
            super(rule);
        }

        @Override
        protected ConceptBuilder getThis() {
            return this;
        }

        public ConceptBuilder providedConcepts(Map<String, Activation> providedConcepts) {
            Concept r = build();
            r.providedConcepts.putAll(providedConcepts);
            return this;
        }
    }

    public static ConceptBuilder builder() {
        return new ConceptBuilder(new Concept());
    }
}
