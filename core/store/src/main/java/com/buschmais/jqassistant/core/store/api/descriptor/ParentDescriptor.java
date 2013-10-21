package com.buschmais.jqassistant.core.store.api.descriptor;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base implementation of an
 * {@link AbstractFullQualifiedNameDescriptor} which contains other
 * {@link Descriptor}s.
 */
public abstract class ParentDescriptor extends AbstractFullQualifiedNameDescriptor {

	/**
	 * The contained descriptors.
	 */
	private Set<Descriptor> contains = new HashSet<>();

	/**
	 * Return the contained descriptors.
	 * 
	 * @return The contained descriptors.
	 */
	public Set<Descriptor> getContains() {
		return contains;
	}

	/**
	 * Set the contained descriptors.
	 * 
	 * @param contains
	 *            The contained descriptors.
	 */
	public void setContains(Set<Descriptor> contains) {
		this.contains = contains;
	}

}
