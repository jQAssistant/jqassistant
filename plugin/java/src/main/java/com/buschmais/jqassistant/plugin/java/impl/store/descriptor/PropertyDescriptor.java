package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;

/**
 * Represents a property value.
 */
@Label("PROPERTY")
public interface PropertyDescriptor extends ValueDescriptor<String> {

    @Property("VALUE")
    @Override
    String getValue();

    @Override
    void setValue(String value);

}
