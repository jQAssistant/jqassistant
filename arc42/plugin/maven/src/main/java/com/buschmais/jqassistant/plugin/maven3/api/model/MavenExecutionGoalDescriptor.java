package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for plugin execution goals.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
@Label("ExecutionGoal")
public interface MavenExecutionGoalDescriptor extends MavenDescriptor {

    /**
     * Get the name of the goal.
     * 
     * @return The name.
     */
    @Property("name")
    String getName();

    /**
     * Set the name of the goal.
     * 
     * @param name
     *            the name.
     */
    void setName(String name);
}
