package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Represents an enumeration value.
 */
public interface EnumValueDescriptor extends TypedDescriptor, ValueDescriptor<FieldDescriptor>, EnumDescriptor {

    @Relation("HAS")
    @Override
    FieldDescriptor getValue();

    @Override
    void setValue(FieldDescriptor fieldDescriptor);
}
