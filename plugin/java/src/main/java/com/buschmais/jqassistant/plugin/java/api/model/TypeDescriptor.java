package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.generics.TypeVariableDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Describes a Java type.
 */
@Label(value = "Type", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends JavaByteCodeDescriptor, PackageMemberDescriptor {

    /**
     * Resolve a required {@link TypeVariableDescriptor} by name.
     *
     * @param name
     *            The name.
     * @return The resolved {@link TypeVariableDescriptor}.
     */
    @ResultOf
    @Cypher("MATCH (type:Type) WHERE id(type)=$this MERGE (type)-[:REQUIRES_TYPE_PARAMETER]->(variable:Java:ByteCode:Bound:TypeVariable{name:$name}) RETURN variable")
    TypeVariableDescriptor requireTypeParameter(@Parameter("name") String name);

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
