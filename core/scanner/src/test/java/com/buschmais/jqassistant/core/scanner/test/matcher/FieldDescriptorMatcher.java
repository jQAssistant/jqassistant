package com.buschmais.jqassistant.core.scanner.test.matcher;

import com.buschmais.jqassistant.core.model.api.descriptor.FieldDescriptor;

import java.lang.reflect.Field;

/**
 * A matcher for {@FieldDescriptor}s.
 */
public class FieldDescriptorMatcher extends AbstractDescriptorMatcher<FieldDescriptor> {

    /**
     * Constructor.
     *
     * @param field The expected field.
     */
    protected FieldDescriptorMatcher(Field field) {
        super(FieldDescriptor.class, field.getDeclaringClass().getName() + "#" + field.getType().getCanonicalName() + " " + field.getName());
    }

    /**
     * Return a {@link FieldDescriptorMatcher}.
     *
     * @param type  The class containing the expected field.
     * @param field The name of the expected field.
     * @return The {@link FieldDescriptorMatcher}.
     */
    public static FieldDescriptorMatcher fieldDescriptor(Class<?> type, String field) throws NoSuchFieldException {
        return fieldDescriptor(type.getDeclaredField(field));
    }

    /**
     * Return a {@link FieldDescriptorMatcher}.
     *
     * @param field The expected field.
     * @return The {@link FieldDescriptorMatcher}.
     */
    public static FieldDescriptorMatcher fieldDescriptor(Field field) {
        return new FieldDescriptorMatcher(field);
    }

}
