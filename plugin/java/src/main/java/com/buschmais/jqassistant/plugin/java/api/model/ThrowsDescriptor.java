package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Defines a THROWS relation between a {@link MethodDescriptor} and a {@link TypeDescriptor}.
 */
@Relation("THROWS")
@Java(Java.JavaLanguageElement.Throws)
public interface ThrowsDescriptor extends Descriptor, LineNumberDescriptor {

    @Outgoing
    MethodDescriptor getThrowingMethod();

    @Incoming
    TypeDescriptor getThrownType();

}
