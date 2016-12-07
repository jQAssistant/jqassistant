package com.buschmais.jqassistant.plugin.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

/**
 * Represents an object found in a JSON document.
 */
@Label("Object")
public interface JSONObjectDescriptor extends JSONDescriptor
{
    @Relation("HAS_KEY")
    List<JSONKeyDescriptor> getKeys();
}
