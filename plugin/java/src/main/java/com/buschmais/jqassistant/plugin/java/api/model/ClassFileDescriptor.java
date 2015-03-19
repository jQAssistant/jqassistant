package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.MD5Descriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Java(Java.JavaLanguageElement.Type)
public interface ClassFileDescriptor extends TypeDescriptor, FileDescriptor, DependentDescriptor, AnnotatedDescriptor,
 AccessModifierDescriptor, MD5Descriptor,
        AbstractDescriptor {

    /**
     * Return the super class.
     *
     * @return The super class.
     */
    @Relation("EXTENDS")
    TypeDescriptor getSuperClass();

    /**
     * Set the super class.
     *
     * @param superClass
     *            The super class.
     */
    void setSuperClass(TypeDescriptor superClass);

    /**
     * Return the implemented interfaces.
     *
     * @return The implemented interfaces.
     */
    @Relation("IMPLEMENTS")
    List<TypeDescriptor> getInterfaces();

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
