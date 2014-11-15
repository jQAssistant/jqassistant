package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * A descriptor representing a property file.
 */
@Label(value = "Properties")
public interface PropertyFileDescriptor extends JavaDescriptor, FileDescriptor {

    @Relation("HAS")
    List<PropertyDescriptor> getProperties();

}
