package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Descriptor for a repository configured for a project.
 *
 * @see http://maven.apache.org/pom.html#Repositories[Repositories in a Maven POM^]
 */
@Label("Repository")
public interface MavenRespositoryDescriptor
    extends MavenDescriptor {

    void setId(String id);

    String getId();

    void setLayout(String layout);

    String getLayout();

    void setName(String name);

    String getName();

    void setURL(String url);

    String getURL();

    void setReleasesEnabled(boolean enabled);

    boolean getReleasesEnabled();

    void setReleasesUpdatePolicy(String policy);

    String getReleasesUpdatePolicy();

    void setReleasesChecksumPolicy(String policy);

    String getReleasesChecksumPolicy();

    void setSnapshotsEnabled(boolean enabled);

    boolean getSnapshotsEnabled();

    void setSnapshotsUpdatePolicy(String policy);

    String getSnapshotsUpdatePolicy();

    void setSnapshotsChecksumPolicy(String policy);

    String getSnapshotsChecksumPolicy();
}
