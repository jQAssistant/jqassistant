package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Relation;

/**
 * Represents an enumeration value.
 */
public interface EnumValueDescriptor extends TypedValueDescriptor<FieldDescriptor>, EnumDescriptor {

    @Relation("HAS")
    @Override
    FieldDescriptor getValue();

    @Override
    void setValue(FieldDescriptor fieldDescriptor);
}
