package com.buschmais.jqassistant.store.impl;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.store.api.model.Descriptor;

public class DescriptorCache<D extends Descriptor> {

	private final Map<String, D> cache = new HashMap<String, D>();

	public interface DescriptorFactory<D> {

		public D create();

	}

	public D get(String fullQualifiedName, DescriptorFactory<D> factory) {
		D descriptor = cache.get(fullQualifiedName);
		if (descriptor == null) {
			descriptor = factory.create();
			if (descriptor != null) {
				cache.put(fullQualifiedName, descriptor);
			}
		}
		return descriptor;
	}
}
