package com.buschmais.jqassistant.core.model.test.matcher.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.ValueDescriptor;
import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.lang.annotation.Annotation;

/**
 * A matcher for {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor}s.
 */
public class ValueDescriptorMatcher<T extends ValueDescriptor> extends TypeSafeMatcher<T> {

    private String expectedName;
    private Character separator;
    private Matcher<T> valueMatcher;

    protected ValueDescriptorMatcher(String expectedName, Character separator, Matcher<T> valueMatcher) {
        this.expectedName = expectedName;
        this.separator = separator;
        this.valueMatcher = valueMatcher;
    }

    @Override
    protected boolean matchesSafely(T item) {
        String name;
        if (separator != null) {
            String fullQualifiedName = item.getFullQualifiedName();
            name = fullQualifiedName.substring(fullQualifiedName.lastIndexOf(separator) + 1);
        } else {
            name = item.getName();
        }
        return (expectedName == null || expectedName.equals(name)) && valueMatcher.matches(item.getValue());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText("a value with name '").appendText(this.expectedName).appendText("' and value '");
        valueMatcher.describeTo(description);
        description.appendText("'");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText("a value with name '").appendText(separator != null ? item.getFullQualifiedName() : item.getName()).appendText("' and value '");
        valueMatcher.describeMismatch(item, mismatchDescription);
        mismatchDescription.appendText("'");
    }

    /**
     * Return a {@link ValueDescriptorMatcher} for annotations.
     *
     * @param annotation   The expected annotation
     * @param valueMatcher The matcher for the annotation values.
     * @return The {@link ValueDescriptorMatcher}.
     */
    public static Matcher<? super AnnotationValueDescriptor> annotationValueDescriptor(Class<? extends Annotation> annotation, Matcher<?> valueMatcher) throws NoSuchFieldException {
        return new ValueDescriptorMatcher(annotation.getName(), '@', valueMatcher);
    }

    /**
     * Return a {@link ValueDescriptorMatcher} for a named valued.
     *
     * @param name         The expected name.
     * @param valueMatcher The matcher for the value.
     * @return The {@link ValueDescriptorMatcher}.
     */
    public static <T> Matcher<? super ValueDescriptor> valueDescriptor(String name, Matcher<T> valueMatcher) {
        return new ValueDescriptorMatcher(name, null, valueMatcher);
    }
}
