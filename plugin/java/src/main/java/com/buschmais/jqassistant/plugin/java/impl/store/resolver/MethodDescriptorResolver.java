package com.buschmais.jqassistant.plugin.java.impl.store.resolver;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor;

public class MethodDescriptorResolver extends AbstractDescriptorResolver<TypeDescriptor, MethodDescriptor> {

	public MethodDescriptorResolver(Store store, TypeDescriptorResolver parentResolver) {
		super(store, parentResolver);
	}

	@Override
	protected Class<MethodDescriptor> getBaseType() {
		return MethodDescriptor.class;
	}

	@Override
	protected char getSeparator() {
		return '#';
	}
}
