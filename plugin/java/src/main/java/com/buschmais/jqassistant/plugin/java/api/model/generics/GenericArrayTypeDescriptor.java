package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("GenericArrayType")
public interface GenericArrayTypeDescriptor extends GenericTypeDescriptor {

    @Relation("HAS_COMPONENT_TYPE")
    GenericTypeDescriptor getComponentType();

    void setComponentType(GenericTypeDescriptor componentType);
}
