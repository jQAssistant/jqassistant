package com.buschmais.jqassistant.store.api.model.descriptor;

import java.util.Set;

/**
 * Interface describing an {@link AbstractDescriptor} which is annotated by other
 * {@link ClassDescriptor}s.
 */
public interface AnnotatedDescriptor {

    /**
     * Return the classes this descriptor is annotated by.
     *
     * @return The classes this descriptor is annotated by.
     */
    Set<ClassDescriptor> getAnnotatedBy();

    /**
     * Set the classes this descriptor is annotated by.
     *
     * @param annotations The classes this descriptor is annotated by.
     */
    void setAnnotatedBy(Set<ClassDescriptor> annotations);

}
