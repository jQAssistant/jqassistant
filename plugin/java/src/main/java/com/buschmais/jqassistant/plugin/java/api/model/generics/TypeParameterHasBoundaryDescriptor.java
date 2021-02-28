package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("HAS_BOUNDARY")
public interface TypeParameterHasBoundaryDescriptor extends Descriptor {

    @Outgoing
    TypeParameterDescriptor getTypeParameter();

    @Incoming
    BoundaryDescriptor getBoundary();

    String getType();

    void setType();

}
