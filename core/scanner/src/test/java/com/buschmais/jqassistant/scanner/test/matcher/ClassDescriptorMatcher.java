package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.core.model.api.descriptor.ClassDescriptor;
import org.hamcrest.Matcher;

/**
 * A matcher for {@link ClassDescriptor}s.
 */
public class ClassDescriptorMatcher extends AbstractDescriptorMatcher<ClassDescriptor> {

    /**
     * Constructor.
     *
     * @param type The expected class.
     */
    protected ClassDescriptorMatcher(Class<?> type) {
        super(ClassDescriptor.class, type.getName());
    }

    /**
     * Constructor.
     *
     * @param name The expected full qualified class name.
     */
    protected ClassDescriptorMatcher(String name) {
        super(ClassDescriptor.class, name);
    }

    /**
     * Return a {@link ClassDescriptorMatcher} .
     *
     * @param type The expected class.
     * @return The {@link ClassDescriptorMatcher}.
     */
    public static Matcher<ClassDescriptor> classDescriptor(Class<?> type) {
        return new ClassDescriptorMatcher(type);
    }

    /**
     * Return a {@link ClassDescriptorMatcher}.
     *
     * @param name The expected full qualified class name.
     * @return The {@link ClassDescriptorMatcher}.
     */
    public static Matcher<ClassDescriptor> classDescriptor(String name) {
        return new ClassDescriptorMatcher(name);
    }
}
