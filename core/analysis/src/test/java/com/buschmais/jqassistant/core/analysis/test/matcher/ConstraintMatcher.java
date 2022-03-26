package com.buschmais.jqassistant.core.analysis.test.matcher;

import com.buschmais.jqassistant.core.rule.api.model.Constraint;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

import org.hamcrest.Matcher;

/**
 * A matcher for {@link Constraint}s.
 *
 * @deprecated This class is replaced by com.buschmais.jqassistant.core.test.matcher.ConstraintMatcher.
 */
@Deprecated
@ToBeRemovedInVersion(major = 1, minor = 13)
public class ConstraintMatcher extends AbstractRuleMatcher<Constraint> {

    /**
     * Constructor.
     *
     * @param id
     *     The expected constraint id.
     */
    protected ConstraintMatcher(String id) {
        super(Constraint.class, id);
    }

    /**
     * Return a {@link ConstraintMatcher}.
     *
     * @param id
     *     The concept id.
     * @return The {@link ConstraintMatcher}.
     */
    public static Matcher<Constraint> constraint(String id) {
        return new ConstraintMatcher(id);
    }
}
