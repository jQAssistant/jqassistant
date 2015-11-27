package com.buschmais.jqassistant.plugin.json.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Array")
public interface JSONArrayValueDescriptor extends JSONValueDescriptor<List<JSONValueDescriptor>> {

    @Relation("CONTAINS_VALUE")
    @Override
    List<JSONValueDescriptor> getValue();

    @Override
    void setValue(List<JSONValueDescriptor> value);
}
