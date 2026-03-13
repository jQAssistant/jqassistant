package com.buschmais.jqassistant.plugin.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Represents a scalar value found in a JSON document.
 */
@Label("Scalar")
public interface JSONScalarValueDescriptor extends JSONValueDescriptor {

    @Property("value")
    Object getValue();

    void setValue(Object value);

}
