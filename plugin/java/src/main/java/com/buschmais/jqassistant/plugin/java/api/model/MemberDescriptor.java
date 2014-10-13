package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor.Declares;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Defines a descriptor having a signature.
 */
public interface MemberDescriptor extends Descriptor {

    @Incoming
    @Declares
    TypeDescriptor getDeclaringType();

    @Property("signature")
    @Indexed
    String getSignature();

    void setSignature(String signature);
}
