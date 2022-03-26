package com.buschmais.jqassistant.core.store.test.matcher;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Abstract base class for descriptor matchers.
 *
 * @deprecated This class is replaced by com.buschmais.jqassistant.core.test.matcher.AbstractDescriptorMatcher.
 */
@Deprecated
@ToBeRemovedInVersion(major = 1, minor = 13)
public class AbstractDescriptorMatcher<T extends FullQualifiedNameDescriptor> extends TypeSafeMatcher<T> {

    private Class<T> type;

    private String fullQualifiedName;

    /**
     * Constructor.
     *
     * @param type
     *            The descriptor types.
     * @param fullQualifiedName
     *            The expected full qualified name.
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
