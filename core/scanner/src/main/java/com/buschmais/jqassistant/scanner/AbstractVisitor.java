package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;

public abstract class AbstractVisitor {

	private final Store store;

	protected AbstractVisitor(Store store) {
		this.store = store;
	}

	protected Store getStore() {
		return store;
	}

	protected ClassDescriptor getClassDescriptor(String typeName) {
		String fullQualifiedName = Type.getObjectType(typeName).getClassName();
		ClassDescriptor classDescriptor = store
				.resolveClassDescriptor(fullQualifiedName);
		return classDescriptor;
	}

	protected void addDependency(DependentDescriptor depentendDescriptor,
			String typeName) {
		if (typeName != null) {
			ClassDescriptor dependency = getClassDescriptor(typeName);
			depentendDescriptor.addDependency(dependency);
		}
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

}
