package com.buschmais.jqassistant.plugin.yaml2.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("Simple")
public interface YMLSimpleKeyDescriptor extends YMLKeyDescriptor {
    // todo rename to key
    @Property("name")
    String getName();

    void setName(String name);

}
