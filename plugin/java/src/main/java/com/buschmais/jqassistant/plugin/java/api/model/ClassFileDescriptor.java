package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.BoundDescriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Java(Java.JavaLanguageElement.Type)
public interface ClassFileDescriptor
        extends TypeDescriptor, FileDescriptor, AnnotatedDescriptor, AccessModifierDescriptor, AbstractDescriptor, ValidDescriptor {

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
     * Return the generic super class.
     *
     * @return The generic super class.
     */
    @Relation("EXTENDS_GENERIC")
    BoundDescriptor getGenericSuperClass();

    /**
     * Set the generic super class.
     *
     * @param genericSuperClass
     *            The generic super class.
     */
    void setGenericSuperClass(BoundDescriptor genericSuperClass);

    /**
     * Return the implemented interfaces.
     *
     * @return The implemented interfaces.
     */
    @Relation("IMPLEMENTS")
    List<TypeDescriptor> getInterfaces();

    /**
     * Return the implemented generic interfaces.
     *
     * @return The implemented generic interfaces.
     */
    @Relation("IMPLEMENTS_GENERIC")
    List<BoundDescriptor> getGenericInterfaces();

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
     *            The name of the source file.
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
     *            The byte code version of the class file.
     */
    void setByteCodeVersion(int byteCodeVersion);
}
