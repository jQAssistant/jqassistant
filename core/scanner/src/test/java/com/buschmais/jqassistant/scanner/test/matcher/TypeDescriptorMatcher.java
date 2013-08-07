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
     * @param name The expected full qualified type name.
     */
    protected TypeDescriptorMatcher(String name) {
        super(TypeDescriptor.class, name);
    }

    /**
     * Return a {@link TypeDescriptorMatcher} .
     *
     * @param type The expected type.
     * @return The {@link TypeDescriptorMatcher}.
     */
    public static Matcher<TypeDescriptor> typeDescriptor(Class<?> type) {
        return new TypeDescriptorMatcher(type);
    }

    /**
     * Return a {@link TypeDescriptorMatcher}.
     *
     * @param name The expected full qualified type name.
     * @return The {@link TypeDescriptorMatcher}.
     */
    public static Matcher<TypeDescriptor> typeDescriptor(String name) {
        return new TypeDescriptorMatcher(name);
    }
}
