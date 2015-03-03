package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.BaseDependencyDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

public interface MavenDependencyDescriptor extends BaseDependencyDescriptor {

    /**
     * Get the artifact dependency.
     *
     * @return The artifact dependency.
     */
    @Incoming
    MavenArtifactDescriptor getDependency();

}
