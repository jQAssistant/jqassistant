package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;

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

	protected String getTypeSignature(final String signature) {
		if (signature != null) {
			SignatureVisitor signatureVisitor = new SignatureVisitor(store);
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
		String fullQualifiedName = typeName.replace("/", ".");
		ClassDescriptor classDescriptor = store
				.getClassDescriptor(fullQualifiedName);
		if (classDescriptor == null) {
			classDescriptor = store.createClassDescriptor(fullQualifiedName);
		}
		return classDescriptor;
	}
}
