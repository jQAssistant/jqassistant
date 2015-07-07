package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Descriptor for relation between a plugin and a dependency.
 */
@Relation("DECLARES_DEPENDENCY")
public interface PluginDependsOnDescriptor extends MavenDependencyDescriptor {

    /**
     * Get the dependent plugin.
     * 
     * @return The dependent plugin.
     */
    @Outgoing
    MavenPluginDescriptor getDependent();

}
