package com.buschmais.jqassistant.plugin.java.test.matcher;

import java.lang.reflect.Field;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;

import org.hamcrest.Matcher;

/**
 * A matcher for {@FieldDescriptor}s.
 */
public class FieldDescriptorMatcher extends AbstractMemberDescriptorMatcher<FieldDescriptor> {

    /**
     * Constructor.
     *
     * @param field
     *            The expected field.
     */
    protected FieldDescriptorMatcher(Field field) {
        super(FieldDescriptor.class, field, field.getType().getTypeName() + " " + field.getName());
    }

    /**
     * Return a {@link FieldDescriptorMatcher}.
     *
     * @param type
     *            The class containing the expected field.
     * @param field
     *            The name of the expected field.
     * @return The {@link FieldDescriptorMatcher}.
     */
    public static Matcher<? super FieldDescriptor> fieldDescriptor(Class<?> type, String field) throws NoSuchFieldException {
        return fieldDescriptor(type.getDeclaredField(field));
    }

    /**
     * Return a {@link FieldDescriptorMatcher}.
     *
     * @param field
     *            The expected field.
     * @return The {@link FieldDescriptorMatcher}.
     */
    public static Matcher<? super FieldDescriptor> fieldDescriptor(Field field) {
        return new FieldDescriptorMatcher(field);
    }

    /**
     * Return a {@link FieldDescriptorMatcher} for an enumeration value.
     *
     * @param enumeration
     *            The expected enumeration.
     * @return The {@link FieldDescriptorMatcher}.
     */
    public static Matcher<? super FieldDescriptor> fieldDescriptor(Enum<? extends Enum<?>> enumeration) throws NoSuchFieldException {
        return fieldDescriptor(enumeration.getClass(), enumeration.name());
    }
}
