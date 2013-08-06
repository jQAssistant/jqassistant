package com.buschmais.jqassistant.scanner.test.matcher;

import com.buschmais.jqassistant.core.model.api.descriptor.MethodDescriptor;

import java.lang.reflect.Method;

/**
 * A matcher for {@link MethodDescriptorMatcher}s.
 */
public class MethodDescriptorMatcher extends AbstractDescriptorMatcher<MethodDescriptor> {

    /**
     * Constructor.
     *
     * @param fqn The expected full qualified name.
     */
    protected MethodDescriptorMatcher(String fqn) {
        super(MethodDescriptor.class, fqn);
    }

    /**
     * Return a {@link MethodDescriptorMatcher}.
     *
     * @param type           The class containing the expected method.
     * @param method         The name of the expected method.
     * @param parameterTypes The parameter types of the expected method.
     * @return The {@link MethodDescriptorMatcher}.
     */
    public static MethodDescriptorMatcher methodDescriptor(Class<?> type, String method, Class<?>... parameterTypes) throws NoSuchMethodException {
        return methodDescriptor(type.getDeclaredMethod(method, parameterTypes));
    }

    /**
     * Return a {@link MethodDescriptorMatcher}.
     *
     * @param method The expected method.
     * @return The {@link MethodDescriptorMatcher}.
     */
    public static MethodDescriptorMatcher methodDescriptor(Method method) {
        StringBuffer name = new StringBuffer();
        name.append(method.getDeclaringClass().getName());
        name.append('#');
        name.append(method.getReturnType().getCanonicalName());
        name.append(' ');
        name.append(method.getName());
        name.append('(');
        int parameterCount = 0;
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterCount > 0) {
                name.append(',');
            }
            name.append(parameterType.getCanonicalName());
            parameterCount++;
        }
        name.append(')');
        return new MethodDescriptorMatcher(name.toString());
    }

}
