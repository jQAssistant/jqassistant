package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor;
import org.hamcrest.Matcher;

/**
 * A matcher for {@link com.buschmais.jqassistant.core.model.api.descriptor.TypeDescriptor}s.
 */
public class TypeDescriptorMatcher extends AbstractDescriptorMatcher<TypeDescriptor> {

    /**
     * Constructor.
     *
     * @param type The expected class.
     */
    protected TypeDescriptorMatcher(Class<?> type) {
        super(TypeDescriptor.class, type.getName());
    }

    /**
     * Constructor.
     *
     * @param name The expected full qualified class name.
     */
    protected TypeDescriptorMatcher(String name) {
        super(TypeDescriptor.class, name);
    }

    /**
     * Return a {@link TypeDescriptorMatcher} .
     *
     * @param type The expected class.
     * @return The {@link TypeDescriptorMatcher}.
     */
    public static Matcher<TypeDescriptor> classDescriptor(Class<?> type) {
        return new TypeDescriptorMatcher(type);
    }

    /**
     * Return a {@link TypeDescriptorMatcher}.
     *
     * @param name The expected full qualified class name.
     * @return The {@link TypeDescriptorMatcher}.
     */
    public static Matcher<TypeDescriptor> classDescriptor(String name) {
        return new TypeDescriptorMatcher(name);
    }
}
