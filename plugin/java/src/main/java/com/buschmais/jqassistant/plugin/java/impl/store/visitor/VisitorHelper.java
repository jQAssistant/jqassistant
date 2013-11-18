package com.buschmais.jqassistant.plugin.java.impl.store.visitor;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.*;
import com.buschmais.jqassistant.plugin.java.impl.store.query.FindParameterQuery;
import com.buschmais.jqassistant.plugin.java.impl.store.query.GetOrCreateFieldQuery;
import com.buschmais.jqassistant.plugin.java.impl.store.query.GetOrCreateMethodQuery;
import com.buschmais.jqassistant.plugin.java.impl.store.resolver.DescriptorResolverFactory;

/**
 * Class containing helper methods for ASM visitors.
 */
public class VisitorHelper {

	/**
	 * The name of constructor methods.
	 */
	private static final String CONSTRUCTOR_METHOD = "void <init>";
	private DescriptorResolverFactory resolverFactory;
	private Store store;

	/**
	 * Constructor.
	 * 
	 * @param store
	 *            The store.
	 * @param resolverFactory
	 *            The resolver factory used for looking up descriptors.
	 */
	public VisitorHelper(Store store, DescriptorResolverFactory resolverFactory) {
		this.store = store;
		this.resolverFactory = resolverFactory;
	}

	/*
	 * Return the type descriptor for the given type name.
	 * 
	 * @param typeName The full qualified name of the type (e.g
	 * java.lang.Object).
	 */
	TypeDescriptor getTypeDescriptor(String typeName) {
		String fullQualifiedName = getType(Type.getObjectType(typeName));
		return resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName);
	}

	/*
	 * Return the type descriptor for the given type name.
	 * 
	 * @param typeName The full qualified name of the type (e.g.
	 * java.lang.Object).
	 * 
	 * @param type The expected type.
	 */
	TypeDescriptor getTypeDescriptor(String typeName, Class<? extends TypeDescriptor> type) {
		String fullQualifiedName = getType(Type.getObjectType(typeName));
		return resolverFactory.getTypeDescriptorResolver().resolve(fullQualifiedName, type);
	}

	/**
	 * Return the method descriptor for the given type and method signature.
	 * 
	 * @param type
	 *            The containing type.
	 * @param signature
	 *            The method signature.
	 * @return The method descriptor.
	 */
	MethodDescriptor getMethodDescriptor(TypeDescriptor type, String signature) {
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);
		params.put("fqn", type.getFullQualifiedName() + "#" + signature);
		params.put("signature", signature);
		MethodDescriptor methodDescriptor = store.executeQuery(GetOrCreateMethodQuery.class, params).getSingleResult()
				.as(GetOrCreateMethodQuery.class).getMethod();
		if (signature.startsWith(CONSTRUCTOR_METHOD) && !ConstructorDescriptor.class.isAssignableFrom(methodDescriptor.getClass())) {
			methodDescriptor = store.migrate(methodDescriptor, ConstructorDescriptor.class);
		}
		return methodDescriptor;
	}

	/**
	 * Return the field descriptor for the given type and field signature.
	 * 
	 * @param type
	 *            The containing type.
	 * @param signature
	 *            The field signature.
	 * @return The field descriptor.
	 */
	FieldDescriptor getFieldDescriptor(TypeDescriptor type, String signature) {
		Map<String, Object> params = new HashMap<>();
		params.put("type", type);
		params.put("fqn", type.getFullQualifiedName() + "#" + signature);
		params.put("signature", signature);
		return store.executeQuery(GetOrCreateFieldQuery.class, params).getSingleResult().as(GetOrCreateFieldQuery.class).getField();
	}

	<T extends ValueDescriptor> T getValueDescriptor(Class<T> type) {
		return store.create(type);
	}

	/**
	 * Add an annotation descriptor of the given type name to an annotated
	 * descriptor.
	 * 
	 * @param annotatedDescriptor
	 *            The annotated descriptor.
	 * @param typeName
	 *            The type name of the annotation.
	 * @return The annotation descriptor.
	 */
	AnnotationValueDescriptor addAnnotation(AnnotatedDescriptor annotatedDescriptor, String typeName) {
		if (typeName != null) {
			TypeDescriptor type = getTypeDescriptor(typeName);
			AnnotationValueDescriptor annotationDescriptor = store.create(AnnotationValueDescriptor.class);
			annotationDescriptor.setType(type);
			annotatedDescriptor.getAnnotatedBy().add(annotationDescriptor);
			return annotationDescriptor;
		}
		return null;
	}

	/**
	 * Create and return the parameter descriptor for the given methodDescriptor
	 * and parameter index.
	 * 
	 * @param methodDescriptor
	 *            The declaring methodDescriptor.
	 * @param index
	 *            The parameter index.
	 * @return The parameter descriptor.
	 */
	ParameterDescriptor addParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
		String fullQualifiedName = methodDescriptor.getFullQualifiedName() + "(" + index + ")";
		ParameterDescriptor parameterDescriptor = store.create(ParameterDescriptor.class, fullQualifiedName);
		methodDescriptor.getParameters().add(parameterDescriptor);
		return parameterDescriptor;
	}

	/**
	 * Return the parameter descriptor for the given methodDescriptor and
	 * parameter index.
	 * 
	 * @param methodDescriptor
	 *            The declaring methodDescriptor.
	 * @param index
	 *            The parameter index.
	 * @return The parameter descriptor.
	 */
	ParameterDescriptor getParameterDescriptor(MethodDescriptor methodDescriptor, int index) {
		Map<String, Object> params = new HashMap<>();
		params.put("method", methodDescriptor);
		params.put("fqn", methodDescriptor.getFullQualifiedName() + "(" + index + ")");
		return store.executeQuery(FindParameterQuery.class, params).getSingleResult().as(FindParameterQuery.class).getParameter();
	}

	/**
	 * Adds a dependency to the given type name to a dependent descriptor.
	 * 
	 * @param dependentDescriptor
	 *            The dependent descriptor.
	 * @param typeName
	 *            The type name of the dependency.
	 */
	void addDependency(DependentDescriptor dependentDescriptor, String typeName) {
		if (typeName != null) {
			TypeDescriptor dependency = getTypeDescriptor(typeName);
			dependentDescriptor.getDependencies().add(dependency);
		}
	}

	/**
	 * Return the type name for the given native name (as provided by ASM).
	 * 
	 * @param desc
	 *            The native name.
	 * @return The type name.
	 */
	String getType(final String desc) {
		return getType(Type.getType(desc));
	}

	/**
	 * Return the type name of the given ASM type.
	 * 
	 * @param t
	 *            The ASM type.
	 * @return The type name.
	 */
	String getType(final Type t) {
		switch (t.getSort()) {
		case Type.ARRAY:
			return getType(t.getElementType());
		default:
			return t.getClassName();
		}
	}

	/**
	 * Return a method signature.
	 * 
	 * @param name
	 *            The method name.
	 * @param desc
	 *            The signature containing parameter, return and exception
	 *            values.
	 * @return The method signature.
	 */
	String getMethodSignature(String name, String desc) {
		StringBuffer signature = new StringBuffer();
		String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
		if (returnType != null) {
			signature.append(returnType);
			signature.append(' ');
		}
		signature.append(name);
		signature.append('(');
		org.objectweb.asm.Type[] types = org.objectweb.asm.Type.getArgumentTypes(desc);
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				signature.append(',');
			}
			signature.append(types[i].getClassName());
		}
		signature.append(')');
		return signature.toString();
	}

	/**
	 * Return a field signature.
	 * 
	 * @param name
	 *            The field name.
	 * @param desc
	 *            The signature containing the type value.
	 * @return The field signature.
	 */
	String getFieldSignature(String name, String desc) {
		StringBuffer signature = new StringBuffer();
		String returnType = org.objectweb.asm.Type.getReturnType(desc).getClassName();
		signature.append(returnType);
		signature.append(' ');
		signature.append(name);
		return signature.toString();
	}
}
