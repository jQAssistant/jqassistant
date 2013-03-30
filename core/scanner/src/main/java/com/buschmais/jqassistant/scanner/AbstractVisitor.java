package com.buschmais.jqassistant.scanner;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

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
		// determine package descriptor
		String[] parts = fullQualifiedName.split("\\.");
		PackageDescriptor parentPackageDescriptor = null;
		int i = 0;
		for (; i < parts.length - 1; i++) {
			PackageDescriptor packageDescriptor = store
					.resolvePackageDescriptor(parentPackageDescriptor, parts[i]);
			if (parentPackageDescriptor != null) {
				parentPackageDescriptor.addChild(packageDescriptor);
			}
			parentPackageDescriptor = packageDescriptor;
		}
		// get class descriptor
		ClassDescriptor classDescriptor = store.resolveClassDescriptor(
				parentPackageDescriptor, parts[i]);
		if (parentPackageDescriptor != null) {
			parentPackageDescriptor.addChild(classDescriptor);
		}
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
