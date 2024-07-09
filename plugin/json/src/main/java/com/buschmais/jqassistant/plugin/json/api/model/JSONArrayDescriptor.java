package com.buschmais.jqassistant.plugin.json.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents an JSON array found within a JSON document.
 */
@Label("Array")
public interface JSONArrayDescriptor extends JSONValueDescriptor {

    @Relation("CONTAINS_VALUE")
    List<JSONDescriptor> getValues();

}
