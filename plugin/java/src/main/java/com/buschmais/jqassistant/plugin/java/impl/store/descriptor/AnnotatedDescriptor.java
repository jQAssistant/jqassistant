package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

import java.util.Set;

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
    @Relation("ANNOTATED_BY")
	Set<AnnotationValueDescriptor> getAnnotatedBy();
}
