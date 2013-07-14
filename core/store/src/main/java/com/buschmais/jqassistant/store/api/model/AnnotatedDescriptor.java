package com.buschmais.jqassistant.store.api.model;

import java.util.Set;

/**
 * Interface describing an {@link com.buschmais.jqassistant.store.api.model.AbstractDescriptor} which is annotated by other
 * {@link com.buschmais.jqassistant.store.api.model.ClassDescriptor}s.
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
