package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;
import java.util.Set;

import org.apache.maven.model.Model;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Descriptor for a pom.xml.
 * 
 * @see Model
 * @author ronald.kunzmann@buschmais.com
 */
@Label(value = "Pom", usingIndexedPropertyOf = FullQualifiedNameDescriptor.class)
public interface MavenPomDescriptor
        extends MavenDescriptor, BaseProfileDescriptor, MavenCoordinatesDescriptor, MavenDependentDescriptor, FullQualifiedNameDescriptor, NamedDescriptor {

    /**
     * Get the artifacts which are described by this POM.
     * 
     * @return The described artifacts.
     */
    @Relation("DESCRIBES")
    Set<ArtifactDescriptor> getDescribes();

    /**
     * Get the parent pom.
     *
     * @return The parent POM.
     */
    @Relation("HAS_PARENT")
    ArtifactDescriptor getParent();

    /**
     * Set the parent POM.
     * 
     * @param parent
     *            The parent POM.
     */
    void setParent(ArtifactDescriptor parent);

    /**
     * Get referenced licenses.
     * 
     * @return The licenses.
     */
    @Relation("USES_LICENSE")
    List<MavenLicenseDescriptor> getLicenses();

    /**
     * Get referenced developers.
     *
     * @return The developers.
     */
    @Relation("HAS_DEVELOPER")
    List<MavenDeveloperDescriptor> getDevelopers();

    /**
     * Returns all mentioned contributors.
     *
     * @return A list of all mentioned contributors.
     */
    @Relation("HAS_CONTRIBUTOR")
    List<MavenContributorDescriptor> getContributors();

    /**
     * Get profile information.
     * 
     * @return The profiles.
     */
    @Relation("HAS_PROFILE")
    List<MavenProfileDescriptor> getProfiles();

    @Outgoing
    List<PomDependsOnDescriptor> getDependencies();

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
    List<PomManagesDependencyDescriptor> getManagedDependencies();

}
