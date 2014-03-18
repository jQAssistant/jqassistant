package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;

import java.util.Set;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Type;

/**
 * Describes a Java type.
 */
@Java(Type)
@Label(value = "TYPE", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends PackageMemberDescriptor, TypeMemberDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor, AbstractDescriptor {

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
     * Return the declared members.
     *
     * @return The declared members.
     */
    @Relation("DECLARES")
    public Set<TypeMemberDescriptor> getDeclaredMembers();
}
