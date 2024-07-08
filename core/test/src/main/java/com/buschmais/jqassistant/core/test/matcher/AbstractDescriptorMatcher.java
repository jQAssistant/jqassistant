package com.buschmais.jqassistant.core.test.matcher;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;

import lombok.RequiredArgsConstructor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static lombok.AccessLevel.PROTECTED;

@RequiredArgsConstructor(access = PROTECTED)
public class AbstractDescriptorMatcher<T extends FullQualifiedNameDescriptor> extends TypeSafeMatcher<T> {

    private final Class<T> type;

    private final String fullQualifiedName;

    @Override
    public boolean matchesSafely(T item) {
        return this.fullQualifiedName.equals(item.getFullQualifiedName());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getSimpleName())
            .appendText("(")
            .appendText(fullQualifiedName)
            .appendText(")");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getClass()
                .getSimpleName())
            .appendText("(")
            .appendText(item.getFullQualifiedName())
            .appendText(")");
    }
}
