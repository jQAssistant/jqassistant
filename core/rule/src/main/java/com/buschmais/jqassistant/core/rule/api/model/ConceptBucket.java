package com.buschmais.jqassistant.core.rule.api.model;

/**
 * Container for a set of unique concepts.
 * <p>
 * A collection of unique rules identified by their id. That means that this bucket
 * cannot contain a concept with the same id twice.
 */
public class ConceptBucket extends AbstractRuleBucket<Concept> {

    @Override
    protected String getRuleTypeName() {
        return "concept";
    }

}
