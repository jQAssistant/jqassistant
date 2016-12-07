package com.buschmais.jqassistant.plugin.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Represents an JSON array found within a JSON document.
 */
@Label("Array")
public interface JSONArrayDescriptor extends JSONValueDescriptor<List<JSONValueDescriptor>> {
    @Relation("CONTAINS_VALUE")
    List<JSONValueDescriptor> getValue();

    @Override
    void setValue(List<JSONValueDescriptor> value);
}
