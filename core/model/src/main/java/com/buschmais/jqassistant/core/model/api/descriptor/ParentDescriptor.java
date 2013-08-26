package com.buschmais.jqassistant.core.model.api.descriptor;

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
    private Set<Descriptor> contains = new HashSet<>();

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    public Set<Descriptor> getContains() {
        return contains;
    }

    /**
     * Set the contained descriptors.
     *
     * @param contains The contained descriptors.
     */
    public void setContains(Set<Descriptor> contains) {
        this.contains = contains;
    }

}
