package com.buschmais.jqassistant.scanner.resolver;

import com.buschmais.jqassistant.store.api.Store;

public class DescriptorResolverFactory {

	private final Store store;

	private final PackageDescriptorResolver packageDescriptorResolver;

	private final ClassDescriptorResolver classDescriptorResolver;

	public DescriptorResolverFactory(Store store) {
		this.store = store;
		packageDescriptorResolver = new PackageDescriptorResolver(store);
		classDescriptorResolver = new ClassDescriptorResolver(store,
				packageDescriptorResolver);
	}

	public Store getStore() {
		return store;
	}

	public PackageDescriptorResolver getPackageDescriptorResolver() {
		return packageDescriptorResolver;
	}

	public ClassDescriptorResolver getClassDescriptorResolver() {
		return classDescriptorResolver;
	}

}
