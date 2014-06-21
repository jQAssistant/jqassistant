package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Defines a descriptor having a signature.
 */
public interface MemberDescriptor extends Descriptor {

    @Incoming
    @TypeDescriptor.Declares
    TypeDescriptor getDeclaringType();

    @Property("signature")
    String getSignature();

    void setSignature(String signature);
}
