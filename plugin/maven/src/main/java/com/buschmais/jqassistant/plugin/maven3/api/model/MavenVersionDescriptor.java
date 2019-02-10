package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label(value = "Version", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface MavenVersionDescriptor extends MavenDescriptor, FullQualifiedNameDescriptor, NamedDescriptor {

    @Relation("CONTAINS")
    List<MavenArtifactDescriptor> getArtifacts();

}
