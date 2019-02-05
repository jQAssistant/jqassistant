package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.AbstractDependencyDescriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * @deprecated This dependency relation and its sub-types do not support
 *             exclusions and will therefore be replaced with
 *             {@link MavenDependencyDescriptor} nodes.
 */
@Abstract
@Deprecated
public interface MavenDependsOnDescriptor extends AbstractDependencyDescriptor {

    /**
     * Get the artifact dependency.
     *
     * @return The artifact dependency.
     */
    @Incoming
    MavenArtifactDescriptor getDependency();

}
