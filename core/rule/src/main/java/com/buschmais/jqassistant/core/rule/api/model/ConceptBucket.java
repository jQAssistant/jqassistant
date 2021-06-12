package com.buschmais.jqassistant.core.rule.api.model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Container for a set of unique concepts.
 * <p>
 * A collection of unique rules identified by their id. That means that this
 * bucket cannot contain a concept with the same id twice.
 */
public class ConceptBucket extends AbstractRuleBucket<Concept> {

    private Map<String, Set<Concept>> providedConcepts = new HashMap();

    @Override
    protected String getRuleTypeName() {
        return "concept";
    }

    @Override
    protected void add(Concept rule) throws RuleException {
        super.add(rule);
        for (String providesConcept : rule.getProvidesConcepts()) {
            providedConcepts.computeIfAbsent(providesConcept, id -> new HashSet<>()).add(rule);
        }
    }

    public Set<Concept> getProvidedConcepts(String id) {
        return providedConcepts.getOrDefault(id, emptySet());
    }
}
