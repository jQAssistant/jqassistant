package com.buschmais.jqassistant.core.model.test.matcher.rule;

import com.buschmais.jqassistant.core.model.api.rule.Rule;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Abstract base class for rule matchers.
 */
public class AbstractRuleMatcher<T extends Rule> extends TypeSafeMatcher<T> {

    private Class<T> type;

    private String id;

    /**
     * Constructor.
     *
     * @param type The rule type.
     * @param id   The expected rule id.
     */
    protected AbstractRuleMatcher(Class<T> type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean matchesSafely(T item) {
        return this.id.equals(item.getId());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getSimpleName()).appendText("(").appendText(id).appendText(")");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getClass().getSimpleName()).appendText("(").appendText(item.getId()).appendText(")");
    }
}