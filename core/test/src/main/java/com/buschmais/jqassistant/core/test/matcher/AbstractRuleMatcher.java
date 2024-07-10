package com.buschmais.jqassistant.core.test.matcher;

import com.buschmais.jqassistant.core.rule.api.model.Rule;

import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static lombok.AccessLevel.PROTECTED;

/**
 * Abstract base class for rules matchers.
 */
@RequiredArgsConstructor(access = PROTECTED)
public class AbstractRuleMatcher<T extends Rule> extends TypeSafeMatcher<T> {

    private final Class<T> type;

    private final String id;

    @Override
    public boolean matchesSafely(T item) {
        return this.id.equals(item.getId());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getSimpleName())
            .appendText("(")
            .appendText(id)
            .appendText(")");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getClass()
                .getSimpleName())
            .appendText("(")
            .appendText(item.getId())
            .appendText(")");
    }
}
