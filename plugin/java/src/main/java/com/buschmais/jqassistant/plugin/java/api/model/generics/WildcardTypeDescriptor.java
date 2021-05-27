package com.buschmais.jqassistant.plugin.java.api.model.generics;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("WildcardType")
public interface WildcardTypeDescriptor extends BoundDescriptor {

    @Relation("HAS_UPPER_BOUND")
    List<BoundDescriptor> getUpperBounds();

    @Relation("HAS_LOWER_BOUND")
    List<BoundDescriptor> getLowerBounds();
}
