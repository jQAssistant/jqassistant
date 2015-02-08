package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import org.apache.maven.model.Model;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.xml.api.model.XmlDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Descriptor for a pom.xml.
 * 
 * @see Model
 * @author ronald.kunzmann@buschmais.com
 */
public interface MavenPomXmlDescriptor extends BaseProfileDescriptor, MavenPomDescriptor, FileDescriptor, XmlDescriptor {

    /**
     * Get the location of the parent project, if one exists. Values from the
     * parent project will be the default for this project if they are left
     * unspecified. The location is given as a group ID, artifact ID and
     * version.
     * 
     * 
     * @return The parent POM.
     */
    @Relation("HAS_PARENT")
    MavenPomDescriptor getParent();

    /**
     * Set the parent POM.
     * 
     * @param parent
     *            The parent POM.
     */
    void setParent(MavenPomDescriptor parent);

    /**
     * Get referenced licenses.
     * 
     * @return The licenses.
     */
    @Relation("USES_LICENSE")
    List<MavenLicenseDescriptor> getLicenses();

    /**
     * Get profile information.
     * 
     * @return The profiles.
     */
    @Relation("HAS_PROFILE")
    List<MavenProfileDescriptor> getProfiles();

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
