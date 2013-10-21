package com.buschmais.jqassistant.core.store.api.descriptor;

/**
 * Abstract implementation of a descriptor.
 */
public abstract class AbstractDescriptor implements Descriptor {

	/**
	 * The unique id of this descriptor.
	 */
	private Long id;

	/**
	 * Return the unique id.
	 * 
	 * @return The unique id.
	 */
	@Override
	public Long getId() {
		return id;
	}

	/**
	 * Set the unique id.
	 * 
	 * @param id
	 *            The unique id.
	 */
	@Override
	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + " [id=" + id + "]";
	}
}
