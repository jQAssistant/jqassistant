package com.buschmais.jqassistant.plugin.java.test.matcher;

import org.hamcrest.Matcher;

import com.buschmais.jqassistant.core.store.test.matcher.AbstractDescriptorMatcher;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

/**
 * A matcher for {@link TypeDescriptor}s.
 */
public class TypeDescriptorMatcher extends AbstractDescriptorMatcher<TypeDescriptor> {

    /**
     * Constructor.
     * 
     * @param type
     *            The expected class.
     */
    protected TypeDescriptorMatcher(Class<?> type) {
        super(TypeDescriptor.class, type.getName());
    }

    /**
     * Constructor.
     * 
     * @param name
     *            The expected full qualified types name.
     */
    protected TypeDescriptorMatcher(String name) {
        super(TypeDescriptor.class, name);
    }

    /**
     * Return a {@link TypeDescriptorMatcher} .
     * 
     * @param type
     *            The expected types.
     * @return The {@link TypeDescriptorMatcher}.
     */
    public static Matcher<? super TypeDescriptor> typeDescriptor(Class<?> type) {
        return new TypeDescriptorMatcher(type);
    }

    /**
     * Return a {@link TypeDescriptorMatcher}.
     * 
     * @param name
     *            The expected full qualified types name.
     * @return The {@link TypeDescriptorMatcher}.
     */
    public static Matcher<? super TypeDescriptor> typeDescriptor(String name) {
        return new TypeDescriptorMatcher(name);
    }
}
