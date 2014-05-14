package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents an array value.
 */
@Label("Array")
public interface ArrayValueDescriptor extends ValueDescriptor<List<ValueDescriptor<?>>> {

    @Relation("HAS")
    @Override
    List<ValueDescriptor<?>> getValue();

    @Override
    void setValue(List<ValueDescriptor<?>> value);

}
