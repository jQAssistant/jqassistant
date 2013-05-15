package com.buschmais.jqassistant.store.api.model;

import java.util.HashSet;
import java.util.Set;

/**
 * Abstract base implementation of an {@link AbstractDescriptor} which contains
 * other {@link AbstractDescriptor}s.
 */
public abstract class ParentDescriptor extends AbstractDescriptor {

	/**
	 * The contained descriptors.
	 */
	private Set<AbstractDescriptor> contains = new HashSet<AbstractDescriptor>();

	/**
	 * Return the contained descriptors.
	 * 
	 * @return The contained descriptors.
	 */
	public Set<AbstractDescriptor> getContains() {
		return contains;
	}

	/**
	 * Set the contained descriptors.
	 * 
	 * @param contains
	 *            The contained descriptors.
	 */
	public void setContains(Set<AbstractDescriptor> contains) {
		this.contains = contains;
	}

}
