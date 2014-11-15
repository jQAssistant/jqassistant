package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 *
 */
@Label("ServiceLoader")
public interface ServiceLoaderDescriptor extends JavaDescriptor, FileDescriptor, TypedDescriptor {

    @Relation("CONTAINS")
    List<TypeDescriptor> getContains();
}
