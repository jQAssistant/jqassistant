package com.buschmais.jqassistant.plugin.common.api.model;

import java.util.Set;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Represents a file.
 */
@Label(value = "File", usingIndexedPropertyOf = FileNameDescriptor.class)
public interface FileDescriptor extends FileNameDescriptor {

    /**
     * Return the parents (directories, artifacts) containing this
     * {@link FileDescriptor}.
     *
     * @return The parents.
     */
    @Incoming
    @Relation("CONTAINS")
    Set<FileDescriptor> getParents();

}
