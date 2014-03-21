package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Relation;
import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;

import java.util.Set;

/**
 * A descriptor representing a property file.
 */
@Label(value = "PROPERTIES", usingIndexedPropertyOf = FileDescriptor.class)
public interface PropertyFileDescriptor extends FileDescriptor {

    @Relation("HAS")
    public Set<PropertyDescriptor> getProperties();

}
