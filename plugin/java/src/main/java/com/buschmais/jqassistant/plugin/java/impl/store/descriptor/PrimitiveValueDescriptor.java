package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Represents a primitive value.
 */
@Label("PRIMITIVE")
public interface PrimitiveValueDescriptor extends TypedDescriptor, ValueDescriptor<Object> {

    @Property("VALUE")
    @Override
    Object getValue();

    @Override
    void setValue(Object value);
}
