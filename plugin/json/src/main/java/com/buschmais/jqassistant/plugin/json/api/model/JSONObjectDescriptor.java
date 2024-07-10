package com.buschmais.jqassistant.plugin.json.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents an object found in a JSON document.
 */
@Label("Object")
public interface JSONObjectDescriptor extends JSONValueDescriptor {

    @Relation("HAS_KEY")
    List<JSONKeyDescriptor> getKeys();
}
