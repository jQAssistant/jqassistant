package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.core.store.api.descriptor.ParentDescriptor;

import java.util.Set;

/**
 * Describes a Java type.
 */
@Label(value="TYPE", usingIndexedPropertyOf=FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends ParentDescriptor, SignatureDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor {

    /**
     * Return the super class.
     *
     * @return The super class.
     */
    @Relation("EXTENDS")
    public TypeDescriptor getSuperClass();

    /**
     * Set the super class.
     *
     * @param superClass The super class.
     */
    public void setSuperClass(TypeDescriptor superClass);

    /**
     * Return the implemented interfaces.
     *
     * @return The implemented interfaces.
     */
    @Relation("IMPLEMENTS")
    public Set<TypeDescriptor> getInterfaces();

    /**
     * @return the abstractClass
     */
    @Property("ABSTRACT")
    public Boolean isAbstract();

    /**
     * @param isAbstract the isAbstract to set
     */
    public void setAbstract(boolean isAbstract);
}
