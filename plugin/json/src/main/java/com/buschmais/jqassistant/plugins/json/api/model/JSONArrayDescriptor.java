package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

@Label("Array")
public interface JSONArrayDescriptor extends JSONValueDescriptor<List<JSONValueDescriptor>>, JSONContainer {
    @Relation("CONTAINS_VALUE")
    List<JSONValueDescriptor> getValue();

    @Override
    void setValue(List<JSONValueDescriptor> value);
}
