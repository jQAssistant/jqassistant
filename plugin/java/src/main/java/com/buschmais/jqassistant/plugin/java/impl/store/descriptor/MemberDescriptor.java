package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.TypeDescriptor.Declares;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Defines a descriptor having a signature.
 */
public interface MemberDescriptor extends Descriptor {

    @Incoming
    @Declares
    TypeDescriptor getDeclaringType();

    @Property("SIGNATURE")
    String getSignature();

    void setSignature(String signature);
}
