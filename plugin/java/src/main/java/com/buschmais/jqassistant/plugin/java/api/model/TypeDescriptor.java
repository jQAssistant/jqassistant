package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Describes a Java type.
 */
@Label(value = "Type", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends JavaDescriptor, PackageMemberDescriptor {

    /**
     * Defines the declares relation used for fields, methods and inner classes.
     */
    @Relation("DECLARES")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface Declares {
    }

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
    @Outgoing
    @Declares
    Set<TypeDescriptor> getDeclaredInnerClasses();

    @Outgoing
    List<TypeDependsOnDescriptor> getDependencies();

    @Incoming
    List<TypeDependsOnDescriptor> getDependents();

}
