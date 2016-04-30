package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;

import com.buschmais.xo.neo4j.api.annotation.Relation;

import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.WriteField;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Defines a WRITES relation between a method and a field.
 */
@Relation("WRITES")
@Java(WriteField)
public interface WritesDescriptor extends Descriptor, LineNumberDescriptor {

    @Outgoing
    MethodDescriptor getMethod();

    @Incoming
    FieldDescriptor getField();

}
