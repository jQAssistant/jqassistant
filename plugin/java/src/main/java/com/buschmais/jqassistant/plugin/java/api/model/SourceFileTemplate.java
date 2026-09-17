package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * A template for files or directories referencing a source {@link FileDescriptor}.
 */
public interface SourceFileTemplate {

    /**
     * Return the {@link FileDescriptor} representing the source file.
     *
     * @return The {@link FileDescriptor} representing the source file.
     */
    @Relation
    FileDescriptor getHasSourceFile();

    /**
     * Set the {@link FileDescriptor} representing the source file.
     *
     * @param sourceFileDescriptor
     *     The {@link FileDescriptor} representing the source file..
     */
    void setHasSourceFile(FileDescriptor sourceFileDescriptor);

}
