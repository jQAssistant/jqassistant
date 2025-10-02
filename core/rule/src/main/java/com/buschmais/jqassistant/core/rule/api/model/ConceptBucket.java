package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashMap;
import java.util.Map;

import lombok.Singular;

/**
 * Container for a set of unique concepts.
 * <p>
 * A collection of unique rules identified by their id. That means that this
 * bucket cannot contain a concept with the same id twice.
 */
public class ConceptBucket extends AbstractRuleBucket<Concept> {

    @Override
    protected String getRuleTypeName() {
        return "concept";
    }

    /**
     * A map containing the ids of the overriding and the overridden concept pairs.
     * Key is the id of the overriding concept, value the id of the overridden concept.
     */
    @Singular
    private Map<String, String> overrides = new HashMap<>();

    public void updateOverrideConcepts(Concept concept){
        if(concept.getOverridesConceptId() != null && !concept.getOverridesConceptId().isEmpty()){
            overrides.put(concept.getId(), concept.getOverridesConceptId());
        }
    }
}
