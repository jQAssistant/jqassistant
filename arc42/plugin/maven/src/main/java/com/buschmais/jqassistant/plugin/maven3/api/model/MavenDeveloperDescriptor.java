package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for a developer entry defined in a pom.xml.
 * 
 * @see org.apache.maven.model.Developer
 * @author christofer.dutz@codecentric.de
 *
 */
@Label("Developer")
public interface MavenDeveloperDescriptor extends MavenProjectParticipantDescriptor {

    /**
     * Get the id of the developer.
     *
     * @return The id.
     */
    @Property("id")
    String getId();

    /**
     * Set the id of the developer.
     *
     * @param id
     *            The id.
     */
    void setId(String id);
}
