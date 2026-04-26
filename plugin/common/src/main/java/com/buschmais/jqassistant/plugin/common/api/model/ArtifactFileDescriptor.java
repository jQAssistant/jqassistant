package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents an artifact directory providing/requiring files
 */
public interface ArtifactFileDescriptor extends ArtifactDescriptor, DirectoryDescriptor {

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
