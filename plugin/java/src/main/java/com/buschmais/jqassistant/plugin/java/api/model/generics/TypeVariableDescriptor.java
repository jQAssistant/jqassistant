package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

@Label("TypeVariable")
public interface TypeVariableDescriptor extends BoundDescriptor, NamedDescriptor {

    @Incoming
    @Relation("DECLARES_TYPE_PARAMETER")
    GenericDeclarationDescriptor getDeclaredBy();

    @Relation("HAS_UPPER_BOUND")
    List<BoundDescriptor> getUpperBounds();

}
