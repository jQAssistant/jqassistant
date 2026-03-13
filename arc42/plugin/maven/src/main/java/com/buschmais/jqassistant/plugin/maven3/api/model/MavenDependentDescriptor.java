package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface MavenDependentDescriptor extends Descriptor {

    @Relation("DECLARES_DEPENDENCY")
    List<MavenDependencyDescriptor> getDeclaresDependencies();

    @Relation("MANAGES_DEPENDENCY")
    List<MavenDependencyDescriptor> getManagesDependencies();
}
