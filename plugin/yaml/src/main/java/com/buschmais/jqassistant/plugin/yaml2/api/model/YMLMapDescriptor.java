package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Map")
public interface YMLMapDescriptor extends YMLDescriptor {

    @Relation("HAS_KEY")
    List<YMLKeyDescriptor> getKeys();

    @Property("index")
    Integer getIndex();

    void setIndex(Integer index);
}
