package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("ParameterizedType")
public interface ParameterizedTypeDescriptor extends BoundDescriptor {

    @Outgoing
    @Relation
    List<HasActualTypeArgumentDescriptor> getActualTypeArguments();

}
