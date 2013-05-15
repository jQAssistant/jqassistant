package com.buschmais.jqassistant.store.impl.dao.mapper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.buschmais.jqassistant.store.api.model.AbstractDescriptor;

/**
 * Abstract base implementation of a {@link DescriptorMapper}.
 * 
 * @param <T>
 *            The type.
 */
public abstract class AbstractDescriptorMapper<T extends AbstractDescriptor>
		implements DescriptorMapper<T> {

	/**
	 * Converts a single descriptor instance to a {@link Set} of descriptors.
	 * <p>
	 * This is a convenience method.
	 * </p>
	 * 
	 * @param descriptor
	 *            The descriptor.
	 * @return The {@link Set} containing the descriptor.
	 */
	protected Set<AbstractDescriptor> asSet(AbstractDescriptor descriptor) {
		if (descriptor != null) {
			Set<AbstractDescriptor> set = new HashSet<AbstractDescriptor>();
			set.add(descriptor);
			return set;
		}
		return Collections.emptySet();
	}

	@Override
	public Long getId(T descriptor) {
		return descriptor.getId();
	}

	@Override
	public void setId(T descriptor, Long id) {
		descriptor.setId(id);
	}

}
