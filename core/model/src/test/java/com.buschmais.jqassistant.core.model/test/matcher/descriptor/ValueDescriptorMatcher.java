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
    private char separator;
    private Matcher<?> valueMatcher;

    protected ValueDescriptorMatcher(String expectedName, char separator, Matcher<?> valueMatcher) {
        this.expectedName = expectedName;
        this.separator = separator;
        this.valueMatcher = valueMatcher;
    }

    @Override
    protected boolean matchesSafely(T item) {
        String fullQualifiedName = item.getFullQualifiedName();
        String name = fullQualifiedName.substring(fullQualifiedName.lastIndexOf(separator) + 1);
        return expectedName.equals(name) && valueMatcher.matches(item.getValue());
    }

    @Override
    public void describeTo(Description description) {
    }

    /**
     * Return a {@link ValueDescriptorMatcher} for annotations.
     *
     * @param annotation   The expected annotation
     * @param valueMatcher The matcher for the annotation values.
     * @return The {@link ValueDescriptorMatcher}.
     */
    public static Matcher<? super AnnotationValueDescriptor> annotationValueDescriptor(Class<? extends Annotation> annotation, Matcher<?> valueMatcher) throws NoSuchFieldException {
        return new ValueDescriptorMatcher<AnnotationValueDescriptor>(annotation.getName(), '@', valueMatcher);
    }

}
