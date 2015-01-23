package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Relation("DEPENDS_ON")
public interface DependsOnDescriptor extends BaseDependencyDescriptor {

    @Outgoing
    ArtifactDescriptor getDependent();

    @Incoming
    ArtifactDescriptor getDependency();

}
