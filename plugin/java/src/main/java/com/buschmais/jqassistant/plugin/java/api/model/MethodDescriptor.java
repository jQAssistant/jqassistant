package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Method;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.type.NamedDescriptor;
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
    @Cypher("match (m),(f) where id(m)={this} and id(f)={target} create (m)-[r:READS{lineNumber:{lineNumber}}]->(f) return r")
    void addReads(@Parameter("target") FieldDescriptor target, @Parameter("lineNumber") int lineNumber);

    Set<WritesDescriptor> getWrites();

    @ResultOf
    @Cypher("match (m),(f) where id(m)={this} and id(f)={target} create (m)-[w:WRITES{lineNumber:{lineNumber}}]->(f) return w")
    void addWrites(@Parameter("target") FieldDescriptor target, @Parameter("lineNumber") int lineNumber);

    @Outgoing
    Set<InvokesDescriptor> getInvokes();

    @Incoming
    Set<InvokesDescriptor> getInvokedBy();

    @ResultOf
    @Cypher("match (m1),(m2) where id(m1)={this} and id(m2)={target} create (m1)-[i:INVOKES{lineNumber:{lineNumber}}]->(m2) return i")
    void addInvokes(@Parameter("target") MethodDescriptor target, @Parameter("lineNumber") int lineNumber);

    @ResultOf
    @Cypher("match (m:Method)-[:HAS]->(p:Parameter) where id(m)={this} and p.index={index} return p as parameter")
    ParameterDescriptor findParameter(@Parameter("index") int index);

    @Property("native")
    Boolean isNative();

    void setNative(Boolean nativeMethod);

}
