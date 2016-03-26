package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Key")
public interface JSONKeyDescriptor extends JSONDescriptor {

    @Relation("HAS_VALUE")
    JSONValueDescriptor<?> getValue();

    void setValue(JSONValueDescriptor<?> descriptor);


    @Property("name")
    String getName();

    void setName(String name);
}
