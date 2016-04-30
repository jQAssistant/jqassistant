package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Defines an INVOKES relation between two methods.
 */
@Relation("INVOKES")
@Java(Java.JavaLanguageElement.MethodInvocation)
public interface InvokesDescriptor extends Descriptor, LineNumberDescriptor {

    @Outgoing
    MethodDescriptor getInvokingMethod();

    @Incoming
    MethodDescriptor getInvokedMethod();

}
