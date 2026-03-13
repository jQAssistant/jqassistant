package com.buschmais.jqassistant.plugin.java.api.model.generics;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.IndexTemplate;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("HAS_ACTUAL_TYPE_ARGUMENT")
public interface HasActualTypeArgumentDescriptor extends IndexTemplate, Descriptor {

    @Outgoing
    ParameterizedTypeDescriptor getParameterizedType();

    @Incoming
    BoundDescriptor getTypeArgument();
}
