package com.buschmais.jqassistant.core.rule.api.model;

import java.util.LinkedHashSet;
import java.util.Set;

import lombok.*;
import org.jqassistant.schema.rule.v2.ReferenceType;

import static com.buschmais.jqassistant.core.rule.api.model.Severity.MINOR;

/**
 * Defines a concept which can be applied.
 * <p>
 * Used to add information to the database.
 */
@Getter
public class Concept extends AbstractExecutableRule {

    public static final Severity DEFAULT_SEVERITY = MINOR;

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
        IF_REQUIRED
    }

    @lombok.Builder
    @Getter
    @RequiredArgsConstructor
    @EqualsAndHashCode
    @ToString
    public static class ProvidedConcept {

        @NonNull
        private final String providingConceptId;

        @NonNull
        private final String providedConceptId;

        @NonNull
        private final Activation activation;
    }

    private final Set<ProvidedConcept> providedConcepts = new LinkedHashSet<>();

    public static class ConceptBuilder extends AbstractExecutableRule.Builder<ConceptBuilder, Concept> {
        private ConceptBuilder(Concept rule) {
            super(rule);
        }

        @Override
        protected ConceptBuilder getThis() {
            return this;
        }

        public ConceptBuilder providedConcept(ProvidedConcept providedConcept) {
            Concept r = build();
            r.providedConcepts.add(providedConcept);
            return this;
        }

        public ConceptBuilder providedConcepts(Set<ProvidedConcept> providedConcepts) {
            Concept r = build();
            r.providedConcepts.addAll(providedConcepts);
            return this;
        }

        public ConceptBuilder overrideConcept(ReferenceType overrideConcept) {
            Concept r = build();
            r.setOverridesConceptId(overrideConcept.getRefId());
            return this;
        }
    }

    public static ConceptBuilder builder() {
        return new ConceptBuilder(new Concept());
    }
}
