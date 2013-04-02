package com.buschmais.jqassistant.scanner.resolver;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.PackageDescriptor;

public class PackageDescriptorResolver extends
		AbstractDescriptorResolver<PackageDescriptor, PackageDescriptor> {

	public PackageDescriptorResolver(Store store) {
		super(store);
	}

	@Override
	public PackageDescriptor create(PackageDescriptor parent, String name) {
		PackageDescriptor packageDescriptor = getStore()
				.resolvePackageDescriptor(parent, name);
		if (parent != null) {
			parent.addChild(packageDescriptor);
		}
		return packageDescriptor;
	}

	@Override
	protected char getSeparator() {
		return '.';
	}

}
