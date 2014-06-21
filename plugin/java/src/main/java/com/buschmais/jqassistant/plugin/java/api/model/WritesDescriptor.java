package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.model.Java.JavaLanguageElement.WriteField;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

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
