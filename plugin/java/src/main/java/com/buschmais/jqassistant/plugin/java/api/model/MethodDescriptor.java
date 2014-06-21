package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Method;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
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
    Set<ParameterDescriptor> getParameters();

    @ResultOf
    @Cypher("match (m),(p) where id(m)={this} and id(p)={parameter} create unique (m)-[:HAS]->(p)")
    void addParameter(@Parameter("parameter") ParameterDescriptor target);

    @Relation("RETURNS")
    TypeDescriptor getReturns();

    void setReturns(TypeDescriptor returns);

    @Relation("HAS_DEFAULT")
    ValueDescriptor<?> getHasDefault();

    void setHasDefault(ValueDescriptor<?> hasDefault);

    @Relation("THROWS")
    Set<TypeDescriptor> getDeclaredThrowables();

    Set<ReadsDescriptor> getReads();

    @ResultOf
    @Cypher("match (m),(f) where id(m)={this} and id(f)={target} create (m)-[r:READS]->(f) return r")
    ReadsDescriptor addReads(@Parameter("target") FieldDescriptor target);

    Set<WritesDescriptor> getWrites();

    @ResultOf
    @Cypher("match (m),(f) where id(m)={this} and id(f)={target} create (m)-[w:WRITES]->(f) return w")
    WritesDescriptor addWrites(@Parameter("target") FieldDescriptor target);

    @Outgoing
    Set<InvokesDescriptor> getInvokes();

    @Incoming
    Set<InvokesDescriptor> getInvokedBy();

    @ResultOf
    @Cypher("match (m1),(m2) where id(m1)={this} and id(m2)={target} create (m1)-[i:INVOKES]->(m2) return i")
    InvokesDescriptor addInvokes(@Parameter("target") MethodDescriptor target);

    @ResultOf
    @Cypher("match (m:Method)-[:HAS]->(p:Parameter) where id(m)={this} and p.index={index} return p as parameter")
    ParameterDescriptor findParameter(@Parameter("index") int index);

    @Property("native")
    Boolean isNative();

    void setNative(Boolean nativeMethod);

}
