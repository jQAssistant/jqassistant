package com.buschmais.jqassistant.plugin.java.test.assertj;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import org.assertj.core.api.Condition;

/**
 * A {@link Condition} for asserting a {@link TypeDescriptor} by its name.
 */
public class TypeDescriptorCondition extends Condition<TypeDescriptor> {

    private final String expectedFullyQualifiedName;

    private TypeDescriptorCondition(String expectedFullyQualifiedName) {
        super("type '" + expectedFullyQualifiedName + "'");
        this.expectedFullyQualifiedName = expectedFullyQualifiedName;
    }

    @Override
    public boolean matches(TypeDescriptor value) {
        return value.getFullQualifiedName()
            .equals(expectedFullyQualifiedName);
    }

    public static TypeDescriptorCondition typeDescriptor(String expectedFullyQualifiedName) {
        return new TypeDescriptorCondition(expectedFullyQualifiedName);
    }

    public static TypeDescriptorCondition typeDescriptor(Class<?> expectedType) {
        return new TypeDescriptorCondition(expectedType.getName());
    }
}
