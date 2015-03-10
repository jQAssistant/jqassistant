package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;

@Java(Java.JavaLanguageElement.Type)
public interface ClassFileDescriptor extends TypeDescriptor, FileDescriptor {

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
     *            The byte code version of the class file.
     */
    void setByteCodeVersion(int byteCodeVersion);
}
