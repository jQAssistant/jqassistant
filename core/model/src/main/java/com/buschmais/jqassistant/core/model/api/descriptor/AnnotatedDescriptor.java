package com.buschmais.jqassistant.core.model.api.descriptor;

import java.util.Set;

/**
 * Interface describing an {@link AbstractDescriptor} which is annotated by other
 * {@link TypeDescriptor}s.
 */
public interface AnnotatedDescriptor {

    /**
     * Return the classes this descriptor is annotated by.
     *
     * @return The classes this descriptor is annotated by.
     */
    Set<TypeDescriptor> getAnnotatedBy();

    /**
     * Set the classes this descriptor is annotated by.
     *
     * @param annotations The classes this descriptor is annotated by.
     */
    void setAnnotatedBy(Set<TypeDescriptor> annotations);

}
