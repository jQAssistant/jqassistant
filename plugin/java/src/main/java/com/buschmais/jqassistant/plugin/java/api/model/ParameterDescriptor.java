package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Describes a parameter of a method.
 */
@Label(value = "Parameter")
public interface ParameterDescriptor extends TypedDescriptor, AnnotatedDescriptor {

    @Property("index")
    int getIndex();

    void setIndex(int index);

}
