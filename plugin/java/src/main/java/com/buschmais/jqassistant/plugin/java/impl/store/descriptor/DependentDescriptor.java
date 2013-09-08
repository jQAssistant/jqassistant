package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.model.api.descriptor.Descriptor;

import java.util.Set;

/**
 * Interface describing a {@link Descriptor} which depends on other
 * {@link TypeDescriptor}s.
 */
public interface DependentDescriptor extends Descriptor {

    /**
     * Return the classes this descriptor depends on.
     *
     * @return The classes this descriptor depends on.
     */
    Set<TypeDescriptor> getDependencies();

    /**
     * Set the classes this descriptor depends on.
     *
     * @param dependencies The classes this descriptor depends on.
     */
    void setDependencies(Set<TypeDescriptor> dependencies);

}
