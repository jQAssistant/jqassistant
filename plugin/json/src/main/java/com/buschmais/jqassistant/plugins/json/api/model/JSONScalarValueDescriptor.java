package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

@Label("Scalar")
public interface JSONScalarValueDescriptor extends JSONValueDescriptor<Object> {

    @Property("value")
    @Override
    void setValue(Object value);

    @Override
    Object getValue();
}
