package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.Type;
import static com.buschmais.xo.api.annotation.ResultOf.Parameter;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.FullQualifiedNameDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes a Java type.
 */
@Java(Type)
@Label(value = "TYPE", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface TypeDescriptor extends PackageMemberDescriptor, DependentDescriptor, AnnotatedDescriptor, AccessModifierDescriptor, AbstractDescriptor {

    @Relation("DECLARES")
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Declares {
    }

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
     * @param superClass
     *            The super class.
     */
    public void setSuperClass(TypeDescriptor superClass);

    /**
     * Return the implemented interfaces.
     * 
     * @return The implemented interfaces.
     */
    @Relation("IMPLEMENTS")
    public Set<TypeDescriptor> getInterfaces();

    @ResultOf
    @Cypher("match (t),(i) where id(t)={this} and id(i)={i} create unique (t)-[:IMPLEMENTS]->(i)")
    public void addInterface(@Parameter("i") TypeDescriptor i);

    /**
     * Return the declared methods.
     * 
     * @return The declared methods.
     */
    @Outgoing
    @Declares
    public Set<MethodDescriptor> getDeclaredMethods();

    @ResultOf
    @Cypher("match (t),(m) where id(t)={this} and id(m)={method} create unique (t)-[:DECLARES]->(m)")
    public void addDeclaredMethod(@Parameter("method") MethodDescriptor method);

    /**
     * Return the declared fields.
     * 
     * @return The declared fields.
     */
    @Outgoing
    @Declares
    public Set<FieldDescriptor> getDeclaredFields();

    @ResultOf
    @Cypher("match (t),(f) where id(t)={this} and id(f)={field} create unique (t)-[:DECLARES]->(f)")
    public void addDeclaredField(@Parameter("field") FieldDescriptor field);

    /**
     * Return the declared inner classes.
     * 
     * @return The declared inner classes.
     */
    @Relation("DECLARES")
    public Set<TypeDescriptor> getDeclaredInnerClasses();

    @ResultOf
    @Cypher("match (t),(i) where id(t)={this} and id(i)={innerClass} create unique (t)-[:DECLARES]->(i)")
    public void addDeclaredInnerClass(@Parameter("innerClass") TypeDescriptor innerClass);

    @ResultOf
    @Cypher("match (t:TYPE) where id(t)={this} create unique (t)-[:DECLARES]->(f:FIELD {SIGNATURE:{signature}}) return f as field")
    public FieldDescriptor getOrCreateField(@Parameter("signature") String signature);

    @ResultOf
    @Cypher("match (t:TYPE) where id(t)={this} create unique (t)-[:DECLARES]->(m:METHOD {SIGNATURE:{signature}}) return m as method")
    public MethodDescriptor getOrCreateMethod(@Parameter("signature") String signature);
}
