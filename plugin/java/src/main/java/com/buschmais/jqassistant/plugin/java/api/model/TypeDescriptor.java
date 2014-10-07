package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.api.annotation.ResultOf.Parameter;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.type.FullQualifiedNameDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes a Java type.
 */
@Label(value = "Type", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends PackageMemberDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor, AbstractDescriptor {

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
    Set<TypeDescriptor> getInterfaces();

    @ResultOf
    @Cypher("match (t),(i) where id(t)={this} and id(i)={i} create unique (t)-[:IMPLEMENTS]->(i)")
    void addInterface(@Parameter("i") TypeDescriptor i);

    /**
     * Return the declared methods.
     * 
     * @return The declared methods.
     */
    @Outgoing
    @Declares
    Set<MethodDescriptor> getDeclaredMethods();

    @ResultOf
    @Cypher("match (t),(m) where id(t)={this} and id(m)={method} create unique (t)-[:DECLARES]->(m)")
    void addDeclaredMethod(@Parameter("method") MethodDescriptor method);

    /**
     * Return the declared fields.
     * 
     * @return The declared fields.
     */
    @Outgoing
    @Declares
    Set<FieldDescriptor> getDeclaredFields();

    @ResultOf
    @Cypher("match (t),(f) where id(t)={this} and id(f)={field} create unique (t)-[:DECLARES]->(f)")
    void addDeclaredField(@Parameter("field") FieldDescriptor field);

    /**
     * Return the declared inner classes.
     * 
     * @return The declared inner classes.
     */
    @Relation("DECLARES")
    Set<TypeDescriptor> getDeclaredInnerClasses();

    @ResultOf
    @Cypher("match (t),(i) where id(t)={this} and id(i)={innerClass} create unique (t)-[:DECLARES]->(i)")
    void addDeclaredInnerClass(@Parameter("innerClass") TypeDescriptor innerClass);

    @ResultOf
    @Cypher("match (t:Type) where id(t)={this} create unique (t)-[:DECLARES]->(f:Field {signature:{signature}}) return f as field")
    FieldDescriptor getOrCreateField(@Parameter("signature") String signature);

    @ResultOf
    @Cypher("match (t:Type) where id(t)={this} create unique (t)-[:DECLARES]->(m:Method {signature:{signature}}) return m as method")
    MethodDescriptor getOrCreateMethod(@Parameter("signature") String signature);

    @ResultOf
    @Cypher("match (t:Type) where id(t)={this} create unique (t)-[:DECLARES]->(m:Method:Constructor {signature:{signature}}) return m as method")
    MethodDescriptor getOrCreateConstructor(@Parameter("signature") String signature);
}
