package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Directory")
public interface DirectoryDescriptor extends FileDescriptor {

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    @Relation("CONTAINS")
    List<FileDescriptor> getContains();

}
