package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("REQUIRES")
public interface RequiresDescriptor extends AccessModifierDescriptor, Descriptor {

    @Outgoing
    ModuleDescriptor getRequiringModule();

    @Incoming
    ModuleDescriptor getRequiredModule();
}
