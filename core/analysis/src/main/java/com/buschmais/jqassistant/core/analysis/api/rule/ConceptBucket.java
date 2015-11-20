package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collection;
import java.util.Set;

/**
 * Container for a set of unique concepts.
 *
 * <p>
 *     A collection of unique rules identified by their id. That means that this bucket
 *     cannot contain a concept with the same id twice.
 * </p>
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
