package com.buschmais.jqassistant.core.test.matcher;

import com.buschmais.jqassistant.core.rule.api.model.Constraint;

/**
 * A matcher for {@link Constraint}s.
 */
public class ConstraintMatcher extends com.buschmais.jqassistant.core.analysis.test.matcher.ConstraintMatcher {

    /**
     * Constructor.
     *
     * @param id
     *     The expected constraint id.
     */
    protected ConstraintMatcher(String id) {
        super(id);
    }
}
