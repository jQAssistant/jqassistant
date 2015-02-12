package com.buschmais.jqassistant.plugin.java.api.model;

import static com.buschmais.jqassistant.plugin.java.api.report.Java.JavaLanguageElement.ReadField;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import static com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.report.Java;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Defines a READs relation between a method and a field.
 */
@Relation("READS")
@Java(ReadField)
public interface ReadsDescriptor extends Descriptor, LineNumberDescriptor {

    @Outgoing
    MethodDescriptor getMethod();

    @Incoming
    FieldDescriptor getField();

}
