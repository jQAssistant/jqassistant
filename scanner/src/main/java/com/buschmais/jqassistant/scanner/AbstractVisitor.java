package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;
import com.buschmais.jqassistant.store.api.model.FieldDescriptor;
import com.buschmais.jqassistant.store.api.model.MethodDescriptor;

public abstract class AbstractVisitor {

	private final Store store;

	protected AbstractVisitor(Store store) {
		this.store = store;
	}

	protected Store getStore() {
		return store;
	}

	// utility methods

	protected String getInternalName(final String name) {
		if (name != null) {
			return getType(Type.getObjectType(name));
		}
		return null;
	}

	protected String getType(final String desc) {
		return getType(Type.getType(desc));
	}

	protected String getType(final Type t) {
		switch (t.getSort()) {
		case Type.ARRAY:
			return getType(t.getElementType());
		default:
			return t.getClassName();
		}
	}

	protected void addDependency(DependentDescriptor depentendDescriptor,
			String typeName) {
		if (typeName != null) {
			ClassDescriptor dependency = getClassDescriptor(typeName);
			depentendDescriptor.addDependency(dependency);
		}
	}

	protected ClassDescriptor getClassDescriptor(String typeName) {
		String fullQualifiedName = Type.getObjectType(typeName).getClassName();
		ClassDescriptor classDescriptor = store
				.getClassDescriptor(fullQualifiedName);
		if (classDescriptor == null) {
			classDescriptor = store.createClassDescriptor(fullQualifiedName);
		}
		return classDescriptor;
	}

	protected MethodDescriptor getMethodDescriptor(
			ClassDescriptor classDescriptor, String name, String desc) {
		String fullQualifiedName = classDescriptor.getFullQualifiedName() + "#"
				+ getMethodSignature(name, desc);
		MethodDescriptor methodDescriptor = getStore().getMethodDescriptor(
				fullQualifiedName);
		if (methodDescriptor == null) {
			methodDescriptor = store.createMethodDescriptor(fullQualifiedName);
		}
		return methodDescriptor;
	}

	protected FieldDescriptor getFielDescriptor(
			ClassDescriptor classDescriptor, String name, String desc) {
		String fullQualifiedName = classDescriptor.getFullQualifiedName() + "#"
				+ getFieldSignature(name, desc);
		FieldDescriptor fieldDescriptor = getStore().getFieldDescriptor(
				fullQualifiedName);
		if (fieldDescriptor == null) {
			fieldDescriptor = store.createFieldDescriptor(fullQualifiedName);
		}
		return fieldDescriptor;
	}

	protected void addDependency(MethodDescriptor methodDescriptor,
			String typeName) {
		if (typeName != null) {
			ClassDescriptor dependency = getClassDescriptor(typeName);
			methodDescriptor.addDependency(dependency);
		}
	}

	private String getMethodSignature(String name, String desc) {
		StringBuffer signature = new StringBuffer();
		String returnType = getType(Type.getReturnType(desc));
		if (returnType != null) {
			signature.append(returnType);
			signature.append(' ');
		}
		signature.append(name);
		signature.append('(');
		Type[] types = Type.getArgumentTypes(desc);
		for (int i = 0; i < types.length; i++) {
			if (i > 0) {
				signature.append(',');
			}
			signature.append(getType(types[i]));
		}
		signature.append(')');
		return signature.toString();
	}

	private String getFieldSignature(String name, String desc) {
		StringBuffer signature = new StringBuffer();
		String returnType = getType(Type.getReturnType(desc));
		signature.append(returnType);
		signature.append(' ');
		signature.append(name);
		return signature.toString();
	}

}
