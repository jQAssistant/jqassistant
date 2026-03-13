package com.buschmais.jqassistant.plugin.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents a key found in a JSON document.
 */
@Label("Key")
public interface JSONKeyDescriptor extends JSONDescriptor {

    @Relation("HAS_VALUE")
    JSONValueDescriptor getValue();

    void setValue(JSONValueDescriptor value);

    @Property("name")
    String getName();

    void setName(String name);
}
