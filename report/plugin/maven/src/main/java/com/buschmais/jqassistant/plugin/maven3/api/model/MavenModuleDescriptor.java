package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for Maven modules defined in POMs.
 * 
 * @author ronald.kunzmann@buschmais.com
 *
 */
@Label("Module")
public interface MavenModuleDescriptor extends MavenDescriptor {

    /**
     * Get the name of the module.
     * 
     * @return The name.
     */
    @Property("name")
    String getName();

    /**
     * Set the name of the module.
     * 
     * @param name
     *            The name.
     */
    void setName(String name);
}
