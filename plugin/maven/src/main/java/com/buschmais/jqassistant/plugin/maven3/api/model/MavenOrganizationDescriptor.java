package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Descriptor for the organisation behind a project.
 *
 * @see http://maven.apache.org/pom.html#Organization[Organization in a Maven POM^]
 */
@Label("Organization")
public interface MavenOrganizationDescriptor extends MavenDescriptor {
    /**
     * Gets the name of the organisation.
     *
     * @return The name of the organisation.
     */
    @Property("name")
    String getName();

    /**
     * Sets the name of the organization.
     *
     * @param name The name.
     */
    void setName(String name);

    /**
     * Get the URL of the organization.
     *
     * @return The URL of the organisation.
     */
    @Property("url")
    String getUrl();

    /**
     * Set the URL of the organization.
     *
     * @param url The URL of the organization.
     */
    void setUrl(String url);
}
