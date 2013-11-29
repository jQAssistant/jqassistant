package com.buschmais.jqassistant.plugin.java.test.matcher;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.hamcrest.Matcher;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;

/**
 * A matcher for {@link MethodDescriptorMatcher}s.
 */
public class MethodDescriptorMatcher extends AbstractSignatureDescriptorMatcher<MethodDescriptor> {

	/**
	 * Constructor.
	 * 
	 * @param fullQualifiedName
	 *            The expected full qualified name.
	 */
	protected MethodDescriptorMatcher(String fullQualifiedName) {
		super(MethodDescriptor.class, fullQualifiedName);
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
	public static Matcher<? super MethodDescriptor> methodDescriptor(Class<?> type, String method, Class<?>... parameterTypes)
			throws NoSuchMethodException {
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
		StringBuffer signature = new StringBuffer();
		signature.append(method.getReturnType().getCanonicalName());
		signature.append(' ');
		signature.append(method.getName());
		signature.append('(');
		int parameterCount = 0;
		for (Class<?> parameterType : method.getParameterTypes()) {
			if (parameterCount > 0) {
				signature.append(',');
			}
			signature.append(parameterType.getCanonicalName());
			parameterCount++;
		}
		signature.append(')');
		return new MethodDescriptorMatcher(signature.toString());
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
	public static Matcher<? super MethodDescriptor> constructorDescriptor(Class<?> type, Class<?>... parameterTypes)
			throws NoSuchMethodException {
		return methodDescriptor(type.getDeclaredConstructor(parameterTypes));
	}

	/**
	 * Return a {@link MethodDescriptorMatcher} for constructors.
	 * 
	 * @param constructor
	 *            The expected constructor.
	 * @return The {@link MethodDescriptorMatcher}.
	 */
	public static Matcher<? super MethodDescriptor> methodDescriptor(Constructor constructor) {
		StringBuffer signature = new StringBuffer();
		signature.append("void");
		signature.append(' ');
		signature.append("<init>");
		signature.append('(');
		int parameterCount = 0;
		for (Class<?> parameterType : constructor.getParameterTypes()) {
			if (parameterCount > 0) {
				signature.append(',');
			}
			signature.append(parameterType.getCanonicalName());
			parameterCount++;
		}
		signature.append(')');
		return new MethodDescriptorMatcher(signature.toString());
	}
}
