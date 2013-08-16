package com.buschmais.jqassistant.core.scanner.test.matcher;

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
     * @param type The descriptor type.
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
        description.appendText(type.getName()).appendText("(").appendText(fullQualifiedName).appendText(")");
    }
}