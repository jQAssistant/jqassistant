package com.buschmais.jqassistant.core.store.api.type;

import java.util.List;

import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface FileContainerDescriptor extends FileDescriptor {

    /**
     * Return the contained descriptors.
     *
     * @return The contained descriptors.
     */
    @Relation("CONTAINS")
    List<FileDescriptor> getContains();

    @ResultOf
    @Cypher("match (c),(f) where id(c)={this} and id(f)={file} create unique (c)-[:CONTAINS]->(f)")
    void addContains(@ResultOf.Parameter("file") FileDescriptor file);

}
