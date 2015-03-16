package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor.Declares;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Defines a member of Java type.
 */
@Label("Member")
public interface MemberDescriptor extends JavaDescriptor, Descriptor {

    /**
     * Return the declaring type.
     * 
     * @return The declaring type.
     */
    @Incoming
    @Declares
    TypeDescriptor getDeclaringType();

    /**
     * Return the signature.
     * 
     * @return The signature.
     */
    String getSignature();

    void setSignature(String signature);
}
