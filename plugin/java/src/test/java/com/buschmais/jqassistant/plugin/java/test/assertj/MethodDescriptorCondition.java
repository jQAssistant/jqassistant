package com.buschmais.jqassistant.plugin.java.test.assertj;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import org.assertj.core.api.Condition;

import static java.util.stream.Collectors.joining;

/**
 * A {@link Condition} for asserting a {@link TypeDescriptor} by its name.
 */
public class MethodDescriptorCondition extends Condition<MethodDescriptor> {

    private final String expectedTypeName;

    private final String expectedSignature;

    private MethodDescriptorCondition(String expectedTypeName, String expectedSignature) {
        super("method '" + expectedTypeName + "#" + expectedSignature + "'");
        this.expectedTypeName = expectedTypeName;
        this.expectedSignature = expectedSignature;
    }

    @Override
    public boolean matches(MethodDescriptor value) {
        return value.getSignature()
            .equals(expectedSignature) && value.getDeclaringType()
            .getFullQualifiedName()
            .equals(expectedTypeName);
    }

    public static MethodDescriptorCondition methodDescriptor(Class<?> type, String name, Class<?>... parameterTypes) throws NoSuchMethodException {
        Method method = type.getDeclaredMethod(name);
        return new MethodDescriptorCondition(type.getName(), method.getReturnType()
            .getTypeName() + " " + name + getDeclaredParameters(type.getDeclaredConstructor(parameterTypes)));
    }

    public static MethodDescriptorCondition constructorDescriptor(Class<?> type, Class<?>... parameterTypes) throws NoSuchMethodException {
        return new MethodDescriptorCondition(type.getName(), "void <init>" + getDeclaredParameters(type.getDeclaredConstructor(parameterTypes)));
    }

    private static StringBuilder getDeclaredParameters(Executable executable) {
        StringBuilder signature = new StringBuilder().append('(');
        signature.append(Arrays.stream(executable.getParameterTypes())
            .map(Class::getTypeName)
            .collect(joining(",")));
        signature.append(')');
        return signature;
    }
}
