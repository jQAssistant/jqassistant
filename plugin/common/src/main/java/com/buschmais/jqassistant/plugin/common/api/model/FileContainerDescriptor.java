package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * The {@link FileContainerDescriptor} describes a file container providing and {@link FileDescriptor}s and requiring {@link FileDescriptor}s, e.g. directories or archives.
 */
@Label("FileContainer")
public interface FileContainerDescriptor extends DirectoryDescriptor {

    /**
     * Return the provided descriptors.
     *
     * @return The provided descriptors.
     */
    @Relation("PROVIDES")
    List<FileDescriptor> getProvides();

    /**
     * Return the required descriptors.
     *
     * @return The required descriptors.
     */
    @Relation("REQUIRES")
    List<FileDescriptor> getRequires();

}
