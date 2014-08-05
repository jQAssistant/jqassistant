package com.buschmais.jqassistant.core.store.api.type;

import java.util.Set;

import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface FileContainerDescriptor extends FileDescriptor {

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    @Relation("CONTAINS")
    public Set<FileDescriptor> getContains();

}
