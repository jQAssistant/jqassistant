package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("ParameterizedType")
public interface ParameterizedTypeDescriptor extends BoundDescriptor {

    @Outgoing
    @Relation
    List<HasActualTypeArgumentDescriptor> getActualTypeArguments();

    @ResultOf
    @Cypher("MATCH (parameterizedType),(typeArgument) WHERE id(parameterizedType)=$this and id(typeArgument)=$typeArgument MERGE (parameterizedType)-[:HAS_ACTUAL_TYPE_ARGUMENT{index:$index}]->(typeArgument)")
    void addActualTypeArgument(@Parameter("index") int index, @Parameter("typeArgument") BoundDescriptor typeArgument);
}
