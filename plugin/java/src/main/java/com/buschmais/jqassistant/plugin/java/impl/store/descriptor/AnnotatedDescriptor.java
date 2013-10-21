package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * Interface describing an {@link Descriptor} which is annotated by
 * {@link AnnotationValueDescriptor}s.
 */
public interface AnnotatedDescriptor extends Descriptor {

	/**
	 * Return the annotations this descriptor is annotated by.
	 * 
	 * @return The annotations this descriptor is annotated by.
	 */
	Set<AnnotationValueDescriptor> getAnnotatedBy();

	/**
	 * Set the annotations this descriptor is annotated by.
	 * 
	 * @param annotations
	 *            The annotations this descriptor is annotated by.
	 */
	void setAnnotatedBy(Set<AnnotationValueDescriptor> annotations);

}
