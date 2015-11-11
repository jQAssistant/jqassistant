package com.buschmais.jqassistant.core.analysis.api.rule;

import java.util.Collection;
import java.util.List;
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

    public void addConcept(Concept concept) throws DuplicateConceptException {
        add(concept);
    }

    /**
     * Returns a unmodifiable set with all concept ids.
     *
     * @return a set with all concept ids. Result will never be {@code null}.
     */
    public Set<String> getConceptIds() {
        return getRuleIds();
    }

    public Concept getConcept(String id) throws NoConceptException {
        return get(id);
    }

    public void addConcepts(ConceptBucket bucket) throws DuplicateConceptException {
        addAll(bucket);
    }

    public Collection<Concept> getConcepts() {
        return getAll();
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
