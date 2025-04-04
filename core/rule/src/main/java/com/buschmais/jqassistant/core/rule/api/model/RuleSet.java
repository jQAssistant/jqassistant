package com.buschmais.jqassistant.core.rule.api.model;

import java.util.Map;
import java.util.Set;

import static com.buschmais.jqassistant.core.rule.api.model.Concept.ProvidedConcept;

public interface RuleSet {

    ConceptBucket getConceptBucket();

    /**
     * Returns a {@link Map} of concept ids as keys and their {@link ProvidedConcept}s values
     */
    Map<String, Set<ProvidedConcept>> getProvidedConcepts();

    /**
     * Returns a {@link Map} of concept ids as keys and their providing concept ids as values
     */
    Map<String, Set<String>> getProvidingConceptIds();

    ConstraintBucket getConstraintBucket();

    GroupsBucket getGroupsBucket();

}
