package com.buschmais.jqassistant.plugin.java.test.assertj;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import org.assertj.core.api.Condition;

/**
 * A {@link Condition} for asserting a {@link TypeDescriptor} by its name.
 */
public class FieldDescriptorCondition extends Condition<FieldDescriptor> {

    private final String expectedTypeName;

    private final String expectedFieldName;

    private FieldDescriptorCondition(String expectedTypeName, String expectedFieldName) {
        super("field '" + expectedTypeName + "#" + expectedFieldName + "'");
        this.expectedTypeName = expectedTypeName;
        this.expectedFieldName = expectedFieldName;
    }

    @Override
    public boolean matches(FieldDescriptor value) {
        return value.getName()
            .equals(expectedFieldName) && value.getDeclaringType()
            .getFullQualifiedName()
            .equals(expectedTypeName);
    }

    public static FieldDescriptorCondition fieldDescriptor(Class<?> expectedType, String expectedFieldName) {
        return new FieldDescriptorCondition(expectedType.getName(), expectedFieldName);
    }
}
