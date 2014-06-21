package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * A descriptor representing a property file.
 */
@Label(value = "Properties", usingIndexedPropertyOf = FileDescriptor.class)
public interface PropertyFileDescriptor extends FileDescriptor {

    @Relation("HAS")
    Set<PropertyDescriptor> getProperties();

}
