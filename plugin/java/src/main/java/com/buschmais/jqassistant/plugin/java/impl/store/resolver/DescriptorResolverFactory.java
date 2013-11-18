package com.buschmais.jqassistant.plugin.java.impl.store.resolver;

import com.buschmais.jqassistant.core.store.api.Store;

public class DescriptorResolverFactory {

	private PackageDescriptorResolver packageDescriptorResolver;

	private TypeDescriptorResolver typeDescriptorResolver;

	public DescriptorResolverFactory(Store store) {
		packageDescriptorResolver = new PackageDescriptorResolver(store);
		typeDescriptorResolver = new TypeDescriptorResolver(store, packageDescriptorResolver);
	}

	public TypeDescriptorResolver getTypeDescriptorResolver() {
		return typeDescriptorResolver;
	}
}
