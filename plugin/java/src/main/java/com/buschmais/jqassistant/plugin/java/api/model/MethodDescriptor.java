package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.Method;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
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

    @Property("native")
    Boolean isNative();

    void setNative(Boolean nativeMethod);

}
