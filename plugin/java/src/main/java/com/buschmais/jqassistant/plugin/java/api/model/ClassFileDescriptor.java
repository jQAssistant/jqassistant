package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Defines the combination of labels "Java" and "ByteCode".
 */
public interface ClassFileDescriptor extends JavaByteCodeDescriptor, FileDescriptor, FullQualifiedNameDescriptor, AnnotatedDescriptor, ValidDescriptor {

    /**
     * Return the name of the source file.
     *
     * @return The name of the source file.
     */
    String getSourceFileName();

    /**
     * Set the name of the source file.
     *
     * @param sourceFileName
     *     The name of the source file.
     */
    void setSourceFileName(String sourceFileName);

    /**
     * Return the byte code version of the class file.
     *
     * @return The byte code version of the class file.
     */
    int getByteCodeVersion();

    /**
     * Set the byte code version of the class file.
     *
     * @param byteCodeVersion
     *     The byte code version of the class file.
     */
    void setByteCodeVersion(int byteCodeVersion);

    @Relation.Outgoing
    List<ClassFileDependsOnDescriptor> getDependencies();

    @Relation.Incoming
    List<ClassFileDependsOnDescriptor> getDependents();

}
