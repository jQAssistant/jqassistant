package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Defines a member of Java type.
 */
@Label("Member")
public interface MemberDescriptor extends JavaDescriptor, NamedDescriptor, SignatureDescriptor, AnnotatedDescriptor, AccessModifierDescriptor, Descriptor {

    /**
     * Return the declaring type.
     *
     * @return The declaring type.
     */
    @Incoming
    @Declares
    TypeDescriptor getDeclaringType();

}
