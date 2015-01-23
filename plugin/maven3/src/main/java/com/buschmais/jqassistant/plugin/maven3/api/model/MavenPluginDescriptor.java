package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import org.apache.maven.model.Plugin;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor for references build plugins.
 * 
 * @see Plugin
 * @author ronald.kunzmann@buschmais.com
 *
 */
@Label("Plugin")
public interface MavenPluginDescriptor extends MavenArtifactDescriptor, ConfigurableDescriptor {

    /**
     * Get whether any configuration should be propagated to child POMs.
     * 
     * @return true, if configuration should be propagated to child POMs.
     */
    @Property("inherited")
    boolean isInherited();

    /**
     * 
     * Set whether any configuration should be propagated to child POMs.
     * 
     * @param inherited
     *            true, if configuration should be propagated to child POMs.
     */
    void setInherited(boolean inherited);

    /**
     * Get plugin executions.
     * 
     * @return The plugin executions.
     */
    @Relation("HAS_EXECUTION")
    List<MavenPluginExecutionDescriptor> getExecutions();

}
