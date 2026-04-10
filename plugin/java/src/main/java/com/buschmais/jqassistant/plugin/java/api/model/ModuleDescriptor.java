package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

@Label("Module")
public interface ModuleDescriptor extends JavaByteCodeDescriptor, FullQualifiedNameDescriptor {

    @Incoming
    List<RequiresDescriptor> getRequiringModules();

}
