package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("DEPENDS_ON")
public interface TypeDependsOnDescriptor extends Descriptor {

    @Incoming
    TypeDescriptor getDependency();

    @Outgoing
    TypeDescriptor getDependent();

    Integer getWeight();

    void setWeight(Integer weight);

}
