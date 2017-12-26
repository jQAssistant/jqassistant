package com.buschmais.jqassistant.core.analysis.test.matcher;

import com.buschmais.jqassistant.core.analysis.api.rule.Concept;

import org.hamcrest.Matcher;

/**
 * A matcher for {@link Concept}s.
 */
public class ConceptMatcher extends AbstractRuleMatcher<Concept> {

    /**
     * Constructor.
     * 
     * @param id
     *            The expected concept id.
     */
    protected ConceptMatcher(String id) {
        super(Concept.class, id);
    }

    /**
     * Return a {@link ConceptMatcher}.
     * 
     * @param id
     *            The concept id.
     * @return The {@link ConceptMatcher}.
     */
    public static Matcher<? super Concept> concept(String id) {
        return new ConceptMatcher(id);
    }
}
