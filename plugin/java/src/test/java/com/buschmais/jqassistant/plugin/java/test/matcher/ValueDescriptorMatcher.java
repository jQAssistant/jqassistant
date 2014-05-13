package com.buschmais.jqassistant.plugin.java.test.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ValueDescriptor;

/**
 * A matcher for {@link ValueDescriptor}s.
 */
public class ValueDescriptorMatcher<T extends ValueDescriptor<?>> extends TypeSafeMatcher<T> {

    private String expectedName;
    private Matcher<T> valueMatcher;

    protected ValueDescriptorMatcher(String expectedName, Matcher<T> valueMatcher) {
        this.expectedName = expectedName;
        this.valueMatcher = valueMatcher;
    }

    @Override
    protected boolean matchesSafely(T item) {
        return (expectedName == null || expectedName.equals(item.getName())) && valueMatcher.matches(item.getValue());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a value with name '").appendText(this.expectedName).appendText("' and value '");
        valueMatcher.describeTo(description);
        description.appendText("'");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText("a value with name '").appendText(item.getName()).appendText("' and value '");
        valueMatcher.describeMismatch(item, mismatchDescription);
        mismatchDescription.appendText("'");
    }

    /**
     * Return a {@link ValueDescriptorMatcher} for a named valued.
     * 
     * @param name
     *            The expected name.
     * @param valueMatcher
     *            The matcher for the value.
     * @return The {@link ValueDescriptorMatcher}.
     */
    public static <T> Matcher<? super ValueDescriptor<?>> valueDescriptor(String name, Matcher<T> valueMatcher) {
        return new ValueDescriptorMatcher(name, valueMatcher);
    }
}
