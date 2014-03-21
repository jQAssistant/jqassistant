package com.buschmais.jqassistant.core.scanner.api.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Property;
import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * Defines a descriptor having a name.
 */
public interface NamedDescriptor extends Descriptor {

    @Property("NAME")
    String getName();

    void setName(String name);
}
