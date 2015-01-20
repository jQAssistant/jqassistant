package com.buschmais.jqassistant.plugin.m2repo.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Represents an artifact in a maven repository.
 * 
 * @author pherklotz
 */
public interface RepositoryArtifactDescriptor extends MavenDescriptor, ArtifactDescriptor {

    /**
     * The containing repository.
     * 
     * @return the containing repository.
     */
    @Incoming
    ContainsArtifactDescriptor getContainingRepository();
}
