package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Sequence")
public interface YMLSequenceDescriptor extends YMLDescriptor {

    // todo Add relation
    @Relation("HAS_ITEM")
    List<YMLDescriptor> getItems();
}
