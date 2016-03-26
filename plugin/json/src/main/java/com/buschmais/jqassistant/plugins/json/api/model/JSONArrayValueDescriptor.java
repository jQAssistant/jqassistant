package com.buschmais.jqassistant.plugins.json.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Array")
public interface JSONArrayValueDescriptor extends JSONValueDescriptor<List<JSONValueDescriptor>>, JSONContainer {

    @Relation("CONTAINS_VALUE")
    @Override
    List<JSONValueDescriptor> getValue();

    @Override
    void setValue(List<JSONValueDescriptor> value);
}
