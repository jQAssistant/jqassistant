package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Represents a property value.
 */
@Label("Property")
public interface PropertyDescriptor extends ValueDescriptor<String> {

    @Property("value")
    @Override
    String getValue();

    @Override
    void setValue(String value);

}
