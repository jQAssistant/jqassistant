package com.buschmais.jqassistant.plugin.java.test.matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Member;
import java.lang.reflect.Method;

import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;

import org.hamcrest.Matcher;

/**
 * A matcher for {@link MethodDescriptorMatcher}s.
 */
public class MethodDescriptorMatcher extends AbstractMemberDescriptorMatcher<MethodDescriptor> {

    /**
     * Constructor.
     *
     * @param signature
     *            The expected signature.
     */
    protected MethodDescriptorMatcher(Member member, String signature) {
        super(MethodDescriptor.class, member, signature);
    }

    /**
     * Return a {@link MethodDescriptorMatcher}.
     *
     * @param type
     *            The class containing the expected method.
     * @param method
     *            The name of the expected method.
     * @param parameterTypes
     *            The parameter types of the expected method.
     * @return The {@link MethodDescriptorMatcher}.
     */
    public static Matcher<? super MethodDescriptor> methodDescriptor(Class<?> type, String method, Class<?>... parameterTypes) throws NoSuchMethodException {
        return methodDescriptor(type.getDeclaredMethod(method, parameterTypes));
    }

    /**
     * Return a {@link MethodDescriptorMatcher}.
     *
     * @param method
     *            The expected method.
     * @return The {@link MethodDescriptorMatcher}.
     */
    public static Matcher<? super MethodDescriptor> methodDescriptor(Method method) {
        StringBuilder signature = new StringBuilder();
        signature.append(method.getReturnType().getTypeName());
        signature.append(' ');
        signature.append(method.getName());
        signature.append('(');
        int parameterCount = 0;
        for (Class<?> parameterType : method.getParameterTypes()) {
            if (parameterCount > 0) {
                signature.append(',');
            }
            signature.append(parameterType.getTypeName());
            parameterCount++;
        }
        signature.append(')');
        return new MethodDescriptorMatcher(method, signature.toString());
    }

    /**
     * Return a {@link MethodDescriptorMatcher} for constructors.
     *
     * @param type
     *            The class containing the expected constructor.
     * @param parameterTypes
     *            The parameter types of the expected constructor.
     * @return The {@link MethodDescriptorMatcher}.
     */
    public static Matcher<? super MethodDescriptor> constructorDescriptor(Class<?> type, Class<?>... parameterTypes) throws NoSuchMethodException {
        return methodDescriptor(type.getDeclaredConstructor(parameterTypes));
    }

    /**
     * Return a {@link MethodDescriptorMatcher} for constructors.
     *
     * @param constructor
     *            The expected constructor.
     * @return The {@link MethodDescriptorMatcher}.
     */
    public static Matcher<? super MethodDescriptor> methodDescriptor(Constructor<?> constructor) {
        StringBuilder signature = new StringBuilder();
        signature.append("void");
        signature.append(' ');
        signature.append("<init>");
        signature.append('(');
        int parameterCount = 0;
        for (Class<?> parameterType : constructor.getParameterTypes()) {
            if (parameterCount > 0) {
                signature.append(',');
            }
            signature.append(parameterType.getName());
            parameterCount++;
        }
        signature.append(')');
        return new MethodDescriptorMatcher(constructor, signature.toString());
    }
}
