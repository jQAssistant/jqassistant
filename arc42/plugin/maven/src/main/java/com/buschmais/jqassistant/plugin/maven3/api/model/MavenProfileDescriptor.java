package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Descriptor for a POM profile.
 *
 * @author ronald.kunzmann@buschmais.com
 */
@Label("Profile")
public interface MavenProfileDescriptor extends MavenDescriptor, MavenDependentDescriptor, BaseProfileDescriptor {

    @Property("id")
    String getId();

    void setId(String id);

    /**
     * Returns all declared repositories for this profile.
     *
     * @return A list of all declared repositories
     */
    @Relation("HAS_REPOSITORY")
    List<MavenRepositoryDescriptor> getRepositories();

    /**
     * Get information about conditions to activate the profile.
     *
     * @return The activation information.
     */
    @Relation("HAS_ACTIVATION")
    MavenProfileActivationDescriptor getActivation();

    void setActivation(MavenProfileActivationDescriptor activation);
}
