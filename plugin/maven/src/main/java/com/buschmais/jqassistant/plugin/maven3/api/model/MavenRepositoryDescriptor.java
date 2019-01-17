package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.neo4j.api.annotation.*;

/**
 * Descriptor for a repository configured for a project.
 *
 * @see http://maven.apache.org/pom.html#Repositories[Repositories in a Maven
 *      POM^]
 */
@Label("Repository")
public interface MavenRepositoryDescriptor extends MavenDescriptor {

    /**
     * The contained POMs.
     */
    @Relation("CONTAINS_POM")
    List<MavenPomXmlDescriptor> getContainedModels();

    /**
     * The contained artifacts.
     */
    @Relation("CONTAINS_ARTIFACT")
    List<MavenArtifactDescriptor> getContainedArtifacts();

    /**
     * The repository url.
     *
     * @return the repository url.
     */
    @Indexed
    String getUrl();

    /**
     * Set the repository url.
     *
     * @param url
     *            the repository url.
     */
    void setUrl(String url);

    /**
     * The last update.
     *
     * @return the last update.
     */
    @Property("lastUpdate")
    long getLastUpdate();

    /**
     * Set the last update.
     *
     * @param lastUpdate
     *            the last update.
     */
    void setLastUpdate(long lastUpdate);

    void setId(String id);

    String getId();

    void setLayout(String layout);

    String getLayout();

    void setName(String name);

    String getName();

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

    @ResultOf
    @Cypher("MATCH (repository)-[:CONTAINS_POM]->(pom:Maven:Pom:Xml) WHERE id(repository)={this} and pom.fqn={coordinates} RETURN pom")
    Query.Result<MavenPomXmlDescriptor> findModel(@ResultOf.Parameter("coordinates") String coordinates);

    @ResultOf
    @Cypher("MATCH (file:File) WHERE file.fileName={fileName} RETURN file")
    FileDescriptor findFile(@ResultOf.Parameter("fileName") String fileName);

}
