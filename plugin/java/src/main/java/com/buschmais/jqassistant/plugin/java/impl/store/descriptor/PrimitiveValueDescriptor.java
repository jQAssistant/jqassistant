package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;

/**
 * Represents a primitive value.
 */
@Label("PRIMITIVE")
public interface PrimitiveValueDescriptor extends TypedValueDescriptor<Object> {

    @Property("HAS")
    @Override
    Object getValue();

    @Override
    void setValue(Object value);
}
