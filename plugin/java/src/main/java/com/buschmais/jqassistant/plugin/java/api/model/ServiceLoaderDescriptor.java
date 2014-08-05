package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.type.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 *
 */
@Label("ServiceLoader")
public interface ServiceLoaderDescriptor extends FileDescriptor, TypedDescriptor {

    @Relation("CONTAINS")
    Set<TypeDescriptor> getContains();
}
