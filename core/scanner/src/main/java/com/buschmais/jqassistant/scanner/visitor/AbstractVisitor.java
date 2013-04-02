package com.buschmais.jqassistant.scanner.visitor;

import org.objectweb.asm.Type;

import com.buschmais.jqassistant.scanner.resolver.DescriptorResolverFactory;
import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.ClassDescriptor;
import com.buschmais.jqassistant.store.api.model.DependentDescriptor;

public abstract class AbstractVisitor {

	private final DescriptorResolverFactory resolverFactory;

	protected AbstractVisitor(DescriptorResolverFactory resolverFactory) {
		this.resolverFactory = resolverFactory;
	}

	protected DescriptorResolverFactory getResolverFactory() {
		return resolverFactory;
	}

	protected Store getStore() {
		return resolverFactory.getStore();
	}

	protected ClassDescriptor getClassDescriptor(String typeName) {
		String fullQualifiedName = Type.getObjectType(typeName).getClassName();
		return resolverFactory.getClassDescriptorResolver().resolve(
				fullQualifiedName);
	}

	protected void addDependency(DependentDescriptor depentendDescriptor,
			String typeName) {
		if (typeName != null) {
			ClassDescriptor dependency = getClassDescriptor(typeName);
			depentendDescriptor.addDependency(dependency);
		}
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
