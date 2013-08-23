package com.buschmais.jqassistant.core.model.api.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.value.AnnotationValueDescriptor;

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
    Set<AnnotationValueDescriptor> getAnnotatedBy();

    /**
     * Set the annotations this descriptor is annotated by.
     *
     * @param annotations The annotations this descriptor is annotated by.
     */
    void setAnnotatedBy(Set<AnnotationValueDescriptor> annotations);

}
