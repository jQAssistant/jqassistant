package com.buschmais.jqassistant.core.test.matcher;

import com.buschmais.jqassistant.core.rule.api.model.Concept;

/**
 * A matcher for {@link Concept}s.
 */
public class ConceptMatcher extends com.buschmais.jqassistant.core.analysis.test.matcher.ConceptMatcher {

    /**
     * Constructor.
     *
     * @param id
     *     The expected concept id.
     */
    protected ConceptMatcher(String id) {
        super(id);
    }
}
