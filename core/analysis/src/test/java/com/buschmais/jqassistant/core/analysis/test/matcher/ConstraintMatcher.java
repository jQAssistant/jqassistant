package com.buschmais.jqassistant.core.analysis.test.matcher;

import com.buschmais.jqassistant.core.analysis.api.rule.Constraint;

import org.hamcrest.Matcher;

/**
 * A matcher for {@link Constraint}s.
 */
public class ConstraintMatcher extends AbstractRuleMatcher<Constraint> {

    /**
     * Constructor.
     * 
     * @param id
     *            The expected constraint id.
     */
    protected ConstraintMatcher(String id) {
        super(Constraint.class, id);
    }

    /**
     * Return a {@link ConstraintMatcher}.
     * 
     * @param id
     *            The concept id.
     * @return The {@link ConstraintMatcher}.
     */
    public static Matcher<Constraint> constraint(String id) {
        return new ConstraintMatcher(id);
    }
}
