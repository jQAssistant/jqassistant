package com.buschmais.jqassistant.plugin.m2repo.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

public interface RepositoryArtifactDescriptor extends MavenDescriptor, ArtifactDescriptor {

    @Incoming
    ContainsArtifactDescriptor getContainingRepository();
}
