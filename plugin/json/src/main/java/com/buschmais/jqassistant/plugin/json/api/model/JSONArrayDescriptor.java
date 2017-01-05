package com.buschmais.jqassistant.plugin.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Represents an JSON array found within a JSON document.
 */
@Label("Array")
public interface JSONArrayDescriptor extends JSONDescriptor
{
    @Relation("CONTAINS_VALUE")
    List<JSONDescriptor> getValues();

    void setValues(List<JSONDescriptor> value);
}
