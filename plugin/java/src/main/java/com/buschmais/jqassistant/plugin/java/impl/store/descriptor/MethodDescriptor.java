package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.plugin.common.impl.descriptor.NamedDescriptor;

import java.util.Set;

/**
 * Describes a method of a Java class.
 */
@Label(value = "METHOD")
public interface MethodDescriptor extends TypeMemberDescriptor, SignatureDescriptor, NamedDescriptor, DependentDescriptor, AnnotatedDescriptor,
        AccessModifierDescriptor, AbstractDescriptor {

    @Relation("HAS")
    public Set<ParameterDescriptor> getParameters();

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

    @Relation("WRITES")
    public Set<FieldDescriptor> getWrites();

    @Relation("INVOKES")
    public Set<MethodDescriptor> getInvokes();

    @Property("NATIVE")
    public Boolean isNative();

    public void setNative(Boolean nativeMethod);
}
