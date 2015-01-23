package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import org.apache.maven.model.PluginExecution;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor for plugin executions.
 * 
 * @see PluginExecution
 * @author ronald.kunzmann@buschmais.com
 */
@Label("PluginExecution")
public interface MavenPluginExecutionDescriptor extends MavenDescriptor, ConfigurableDescriptor {

    /**
     * Get the identifier of this execution for labelling the goals during the
     * build, and for matching executions to merge during inheritance and
     * profile injection.
     * 
     * @return The id.
     */
    @Property("id")
    String getId();

    /**
     * Sets the id.
     * 
     * @param id
     *            The id.
     */
    void setId(String id);

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
     * Get the build lifecycle phase to bind the goals in this execution to. If
     * omitted, the goals will be bound to the default phase specified in their
     * metadata.
     * 
     * @return The phase.
     */
    @Property("phase")
    String getPhase();

    /**
     * Sets the phase.
     * 
     * @param phase
     *            The phase.
     */
    void setPhase(String phase);

    /**
     * Returns execution goals.
     * 
     * @return The goals.
     */
    @Relation("HAS_GOAL")
    List<MavenExecutionGoalDescriptor> getGoals();

}
