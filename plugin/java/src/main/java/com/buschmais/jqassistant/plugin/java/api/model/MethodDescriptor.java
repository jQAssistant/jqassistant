package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Method;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Describes a method of a Java class.
 */
@Java(Method)
@Label(value = "Method")
public interface MethodDescriptor extends MemberDescriptor, NamedDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor,
        AbstractDescriptor {

    @Relation("HAS")
    List<ParameterDescriptor> getParameters();

    @Relation("RETURNS")
    TypeDescriptor getReturns();

    void setReturns(TypeDescriptor returns);

    @Relation("HAS_DEFAULT")
    ValueDescriptor<?> getHasDefault();

    void setHasDefault(ValueDescriptor<?> hasDefault);

    @Relation("THROWS")
    List<TypeDescriptor> getDeclaredThrowables();

    List<ReadsDescriptor> getReads();

    List<WritesDescriptor> getWrites();

    @Outgoing
    List<InvokesDescriptor> getInvokes();

    @Incoming
    List<InvokesDescriptor> getInvokedBy();

    @ResultOf
    @Cypher("match (m:Method)-[:HAS]->(p:Parameter) where id(m)={this} and p.index={index} return p as parameter")
    ParameterDescriptor findParameter(@Parameter("index") int index);

    @ResultOf
    @Cypher("match (m:Method) where id(m)={this} create unique (m)-[:HAS]->(p:Parameter{index:{index}}) return p")
    ParameterDescriptor createParameter(@Parameter("index") int index);

    @Property("native")
    Boolean isNative();

    void setNative(Boolean nativeMethod);

}
