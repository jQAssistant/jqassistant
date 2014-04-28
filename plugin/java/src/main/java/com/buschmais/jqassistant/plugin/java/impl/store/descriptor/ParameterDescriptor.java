package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Describes a parameter of a method.
 */
@Label(value = "PARAMETER")
public interface ParameterDescriptor extends TypedDescriptor, DependentDescriptor, AnnotatedDescriptor {

    @Property("INDEX")
    int getIndex();

    void setIndex(int index);

}
