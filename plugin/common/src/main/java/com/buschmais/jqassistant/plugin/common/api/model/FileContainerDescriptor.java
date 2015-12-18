package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.List;

import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;


/**
 * The {@link FileContainerDescriptor} describes an file artifact
 * that contains one or more other file artifacts. Typical
 * examples for such artifacts are directories on filesystems
 * or archive formats as Zip, GZip, and Jars.
 */
@Label("Container")
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

    @ResultOf
    @Cypher("MATCH (container)-[:CONTAINS|REQUIRES]->(file:File{fileName:{path}}) WHERE id(container)={this} RETURN file")
    FileDescriptor find(@Parameter("path") String path);

}
