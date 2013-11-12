package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Relation;

/**
 * Represents a class value (e.g. a referenced class).
 */
public interface ClassValueDescriptor extends ValueDescriptor<TypeDescriptor>, ClassDescriptor {

    @Relation("HAS")
    @Override
    TypeDescriptor getValue();

    @Override
    void setValue(TypeDescriptor typeDescriptor);

}
