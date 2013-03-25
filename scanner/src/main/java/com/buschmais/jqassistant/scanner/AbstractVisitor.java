package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.Descriptor;
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
		case Type.OBJECT:
			return t.getInternalName();
		default:
			return null;
		}
	}

	protected String getTypeSignature(final String signature,
			Descriptor descriptor) {
		if (signature != null) {
			SignatureVisitor signatureVisitor = new SignatureVisitor(store,
					descriptor);
			new SignatureReader(signature).acceptType(signatureVisitor);
			return signatureVisitor.getSignatureClassName();
		}
		return null;
	}

	protected void addDependency(ClassDescriptor classDescriptor,
			String typeName) {
		if (typeName != null) {
			ClassDescriptor dependency = getClassDescriptor(typeName);
			classDescriptor.addDependency(dependency);
		}
	}

	protected ClassDescriptor getClassDescriptor(String typeName) {
		String fullQualifiedName = getClassName(typeName);
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
				+ getSignature(name, desc);
		MethodDescriptor methodDescriptor = getStore().getMethodDescriptor(
				fullQualifiedName);
		if (methodDescriptor == null) {
			methodDescriptor = store.createMethodDescriptor(fullQualifiedName);
		}
		return methodDescriptor;
	}

	protected void addDependency(MethodDescriptor methodDescriptor,
			String typeName) {
		if (typeName != null) {
			ClassDescriptor dependency = getClassDescriptor(typeName);
			methodDescriptor.addDependency(dependency);
		}
	}

	private String getSignature(String name, String desc) {
		StringBuffer signature = new StringBuffer();
		String returnType = getClassName(getType(Type.getReturnType(desc)));
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
			signature.append(getClassName(getType(types[i])));
		}
		signature.append(')');
		return signature.toString();
	}

	private String getClassName(String typeName) {
		if (typeName != null) {
			String fullQualifiedName = typeName.replace("/", ".");
			return fullQualifiedName;
		}
		return null;
	}
}
