package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.AbstractDescriptor;

/**
 * Abstract base class for all value descriptors.
 * 
 * @param <V>
 *            The value type.
 */
public abstract class AbstractValueDescriptor<V> extends AbstractDescriptor implements ValueDescriptor<V> {

	private String name;

	private V value;

	@Override
	public String getName() {
		return name;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
}
