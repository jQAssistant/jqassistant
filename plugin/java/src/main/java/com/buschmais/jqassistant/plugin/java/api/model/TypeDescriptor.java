package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor.RequiresType;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Describes a Java type.
 */
@Label(value = "Type", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends JavaDescriptor, PackageMemberDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor,
        AbstractDescriptor {

    @Relation("DECLARES")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Declares {
    }

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
     * Return the declared methods.
     * 
     * @return The declared methods.
     */
    @Outgoing
    @Declares
    List<MethodDescriptor> getDeclaredMethods();

    /**
     * Return the declared fields.
     * 
     * @return The declared fields.
     */
    @Outgoing
    @Declares
    List<FieldDescriptor> getDeclaredFields();

    /**
     * Return the declared members, i.e. fields and methods.
     *
     * @return The declared members.
     */
    @Outgoing
    @Declares
    List<MemberDescriptor> getDeclaredMembers();

    /**
     * Return the declared inner classes.
     * 
     * @return The declared inner classes.
     */
    @Relation("DECLARES")
    List<TypeDescriptor> getDeclaredInnerClasses();

    @Incoming
    @RequiresType
    JavaArtifactFileDescriptor getRequiredBy();

    void setRequiredBy(JavaArtifactFileDescriptor javaArtifact);
}
