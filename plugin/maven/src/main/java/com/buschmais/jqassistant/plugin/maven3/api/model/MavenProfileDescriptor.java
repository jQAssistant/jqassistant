package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Descriptor for a maven profile.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
@Label("Profile")
public interface MavenProfileDescriptor extends MavenDescriptor, MavenDependentDescriptor, BaseProfileDescriptor {

    @Property("id")
    String getId();

    void setId(String id);

    /**
     * Get default dependency information for projects that inherit from this
     * one. The dependencies in this section are not immediately resolved.
     * Instead, when a POM derived from this one declares a dependency described
     * by a matching groupId and artifactId, the version and other values from
     * this section are used for that dependency if they were not already
     * specified.
     * 
     * @return The managed dependencies.
     */
    @Outgoing
    List<ProfileManagesDependencyDescriptor> getManagedDependencies();

    @Outgoing
    List<ProfileDependsOnDescriptor> getDependencies();

    /**
     * Get information about conditions to activate the profile.
     * 
     * @return The activation information.
     */
    @Relation("HAS_ACTIVATION")
    MavenProfileActivationDescriptor getActivation();

    void setActivation(MavenProfileActivationDescriptor activation);

}
