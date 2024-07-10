package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Relation;


/**
 * The {@link FileContainerDescriptor} describes an file artifact
 * that contains one or more other file artifacts. Typical
 * examples for such artifacts are directories on filesystems
 * or archive formats as Zip, GZip, and Jars.
 */
public interface FileContainerDescriptor extends FileNameDescriptor {

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    @Relation("CONTAINS")
    List<FileDescriptor> getContains();

    /**
     * Return the required descriptors.
     *
     * @return The required descriptors.
     */
    @Relation("REQUIRES")
    List<FileDescriptor> getRequires();

}
