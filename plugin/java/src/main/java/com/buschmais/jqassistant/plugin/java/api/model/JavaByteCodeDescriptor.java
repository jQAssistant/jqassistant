package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.api.annotation.Abstract;

/**
 * Defines the combination of labels "Java" and "ByteCode".
 */
@Abstract
public interface JavaByteCodeDescriptor extends JavaDescriptor, ByteCodeDescriptor {

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

}
