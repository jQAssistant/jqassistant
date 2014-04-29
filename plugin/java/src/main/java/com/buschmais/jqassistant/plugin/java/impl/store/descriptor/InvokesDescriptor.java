package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import static com.buschmais.jqassistant.plugin.java.impl.store.descriptor.Java.JavaLanguageElement.MethodInvocation;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Defines an INVOKES relation between two methods.
 */
@Relation("INVOKES")
@Java(MethodInvocation)
public interface InvokesDescriptor extends Descriptor, LineNumberDescriptor {

    @Outgoing
    MethodDescriptor getInvokingMethod();

    @Incoming
    MethodDescriptor getInvokedMethod();


}
