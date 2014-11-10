package com.buschmais.jqassistant.core.store.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface FileContainerDescriptor extends FileNameDescriptor {

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    @Relation("CONTAINS")
    List<FileDescriptor> getContains();

}
