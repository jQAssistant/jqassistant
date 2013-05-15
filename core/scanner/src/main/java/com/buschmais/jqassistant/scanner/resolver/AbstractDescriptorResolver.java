package com.buschmais.jqassistant.scanner.resolver;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.store.api.Store;
import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;

public abstract class AbstractDescriptorResolver<P extends AbstractDescriptor, T extends AbstractDescriptor> {

	private final Store store;

	private final AbstractDescriptorResolver<?, P> parentResolver;

	private final Map<String, T> cachedDescriptors = new HashMap<String, T>();

	protected AbstractDescriptorResolver(Store store,
			AbstractDescriptorResolver<?, P> parentResolver) {
		this.store = store;
		this.parentResolver = parentResolver;
	}

	@SuppressWarnings("unchecked")
	public AbstractDescriptorResolver(Store store) {
		this.store = store;
		this.parentResolver = (AbstractDescriptorResolver<?, P>) this;
	}

	public T resolve(String fullQualifiedName) {
		T descriptor = cachedDescriptors.get(fullQualifiedName);
		if (descriptor == null) {
			String name;
			P parent = null;
			int separatorIndex = fullQualifiedName.lastIndexOf(getSeparator());
			if (separatorIndex == -1) {
				name = fullQualifiedName;
			} else {
				String parentName = fullQualifiedName.substring(0,
						separatorIndex);
				name = fullQualifiedName.substring(separatorIndex + 1,
						fullQualifiedName.length());
				parent = parentResolver.resolve(parentName);
			}
			descriptor = this.create(parent, name);
			this.cachedDescriptors.put(fullQualifiedName, descriptor);
		}
		return descriptor;
	}

	protected Store getStore() {
		return store;
	}

	protected abstract char getSeparator();

	protected abstract T create(P parent, String name);
}
