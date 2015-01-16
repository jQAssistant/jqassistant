package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.BaseDependencyDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("DEPENDS_ON")
public interface ProfileDependsOnDescriptor extends BaseDependencyDescriptor {

    @Outgoing
    MavenProfileDescriptor getDependent();

    @Incoming
    MavenArtifactDescriptor getDependency();

}
