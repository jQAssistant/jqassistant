package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for developer roles.
 * 
 * @author christofer.dutz@codecentric.de
 */
@Label("Role")
public interface MavenParticipantRoleDescriptor extends MavenDescriptor {

    /**
     * Get the name of the role.
     * 
     * @return The name.
     */
    @Property("name")
    String getName();

    /**
     * Set the name of the role.
     * 
     * @param name
     *            the name.
     */
    void setName(String name);
}
