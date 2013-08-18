package com.buschmais.jqassistant.core.model.test.matcher.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.AbstractDescriptor;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Abstract base class for descriptor matchers.
 */
public class AbstractDescriptorMatcher<T extends AbstractDescriptor> extends TypeSafeMatcher<T> {

    private Class<T> type;

    private String fullQualifiedName;

    /**
     * Constructor.
     *
     * @param type              The descriptor types.
     * @param fullQualifiedName The expected full qualified name.
     */
    protected AbstractDescriptorMatcher(Class<T> type, String fullQualifiedName) {
        this.type = type;
        this.fullQualifiedName = fullQualifiedName;
    }

    @Override
    public boolean matchesSafely(T item) {
        return this.fullQualifiedName.equals(item.getFullQualifiedName());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getSimpleName()).appendText("(").appendText(fullQualifiedName).appendText(")");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getClass().getSimpleName()).appendText("(").appendText(item.getFullQualifiedName()).appendText(")");
    }
}