package com.buschmais.jqassistant.store.api.model.descriptor;

import java.util.Set;

/**
 * Interface describing an {@link AbstractDescriptor} which depends on other
 * {@link ClassDescriptor}s.
 */
public interface DependentDescriptor {

    /**
     * Return the classes this descriptor depends on.
     *
     * @return The classes this descriptor depends on.
     */
    Set<ClassDescriptor> getDependencies();

    /**
     * Set the classes this descriptor depends on.
     *
     * @param dependencies The classes this descriptor depends on.
     */
    void setDependencies(Set<ClassDescriptor> dependencies);

}
