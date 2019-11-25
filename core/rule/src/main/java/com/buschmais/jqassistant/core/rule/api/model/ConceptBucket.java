package com.buschmais.jqassistant.core.rule.api.model;

/**
 * Container for a set of unique concepts.
 *
 * A collection of unique rules identified by their id. That means that this bucket
 * cannot contain a concept with the same id twice.
 */
public class ConceptBucket extends AbstractRuleBucket<Concept, NoConceptException, DuplicateConceptException> {

    @Override
    protected String getRuleTypeName() {
        return "concept";
    }

    @Override
    protected DuplicateConceptException newDuplicateRuleException(String message) {
        return new DuplicateConceptException(message);
    }

    @Override
    protected NoConceptException newNoRuleException(String message) {
        return new NoConceptException(message);
    }
}
