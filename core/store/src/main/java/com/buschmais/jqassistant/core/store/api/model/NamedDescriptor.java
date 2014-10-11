package com.buschmais.jqassistant.core.store.api.model;

import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Defines a descriptor having a name.
 */
public interface NamedDescriptor extends Descriptor {

    @Property("name")
    String getName();

    void setName(String name);
}
