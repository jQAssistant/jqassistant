package com.buschmais.jqassistant.plugin.common.impl.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

import java.util.Set;

/**
 * Abstract base implementation of a {@link com.buschmais.jqassistant.core.store.api.descriptor.Descriptor} which contains other {@link com.buschmais.jqassistant.core.store.api.descriptor.Descriptor}s.
 */
public interface ContainingDescriptor extends Descriptor {

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    @Relation("CONTAINS")
    public Set<Descriptor> getContains();
}
