package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.Set;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Method;
import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor.Declares;
import static com.buschmais.xo.api.annotation.ResultOf.Parameter;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Describes a method of a Java class.
 */
@Java(Method)
@Label(value = "METHOD")
public interface MethodDescriptor extends SignatureDescriptor, NamedDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor, AbstractDescriptor {

    @Incoming
    @Declares
    public TypeDescriptor getDeclaringType();

    @Relation("HAS")
    public Set<ParameterDescriptor> getParameters();

    @ResultOf
    @Cypher("match (m),(p) where id(m)={this} and id(p)={parameter} create unique (m)-[:HAS]->(p)")
    public void addParameter(@Parameter("parameter") ParameterDescriptor target);

    @Relation("RETURNS")
    public TypeDescriptor getReturns();

    public void setReturns(TypeDescriptor returns);

    @Relation("HAS_DEFAULT")
    public ValueDescriptor getHasDefault();

    public void setHasDefault(ValueDescriptor hasDefault);

    @Relation("THROWS")
    public Set<TypeDescriptor> getDeclaredThrowables();

    @Relation("READS")
    public Set<FieldDescriptor> getReads();

    @ResultOf
    @Cypher("match (m),(f) where id(m)={this} and id(f)={target} create unique (m)-[:READS]->(f)")
    public void addReads(@Parameter("target") FieldDescriptor target);

    @Relation("WRITES")
    public Set<FieldDescriptor> getWrites();

    @ResultOf
    @Cypher("match (m),(f) where id(m)={this} and id(f)={target} create unique (m)-[:WRITES]->(f)")
    public void addWrites(@Parameter("target") FieldDescriptor target);

    @Relation("INVOKES")
    public Set<MethodDescriptor> getInvokes();

    @ResultOf
    @Cypher("match (m1),(m2) where id(m1)={this} and id(m2)={target} create unique (m1)-[:INVOKES]->(m2)")
    public void addInvokes(@Parameter("target") MethodDescriptor target);

    @ResultOf
    @Cypher("match (m:METHOD)-[:HAS]->(p:PARAMETER) where id(m)={this} and p.INDEX={index} return p as parameter")
    ParameterDescriptor findParameter(@Parameter("index") int index);

    @Property("NATIVE")
    public Boolean isNative();

    public void setNative(Boolean nativeMethod);

}
