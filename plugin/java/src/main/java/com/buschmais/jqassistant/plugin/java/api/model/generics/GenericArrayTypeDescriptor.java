package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("GenericArrayType")
public interface GenericArrayTypeDescriptor extends BoundDescriptor {

    @Relation("HAS_COMPONENT_TYPE")
    BoundDescriptor getComponentType();

    void setComponentType(BoundDescriptor componentType);

}
