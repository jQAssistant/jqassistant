package com.buschmais.jqassistant.plugin.maven3.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Descriptor for a repository configured for a project.
 *
 * @see http://maven.apache.org/pom.html#Repositories[Repositories in a Maven
 *      POM^]
 */
@Label("Repository")
public interface MavenRepositoryDescriptor extends MavenDescriptor {

    @Relation("CONTAINS_GROUP_ID")
    @Retention(RUNTIME)
    @Target(METHOD)
    @interface ContainsGroupId {
    }

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
    @Cypher("MATCH (file:File) WHERE file.fileName={fileName} RETURN file")
    FileDescriptor findFile(@Parameter("fileName") String fileName);

    @ResultOf
    @Cypher("MATCH (repository)-[:CONTAINS_POM]->(pom:Maven:Pom:Release:Xml) WHERE id(repository)={this} and pom.fqn={coordinates} RETURN pom")
    MavenPomXmlDescriptor findReleaseModel(@Parameter("coordinates") String coordinates);

    @ResultOf
    @Cypher("MATCH (repository)-[:CONTAINS_POM]->(pom:Maven:Pom:Snapshot:Xml) WHERE id(repository)={this} and pom.fqn={coordinates} RETURN pom")
    MavenPomXmlDescriptor findSnapshotModel(@Parameter("coordinates") String coordinates);

    @ResultOf
    @Cypher("MATCH (repository)-[:CONTAINS_ARTIFACT]->(artifact:Artifact) WHERE id(repository)={this} and artifact.fqn={coordinates} RETURN artifact")
    MavenArtifactDescriptor findArtifact(@Parameter("coordinates") String coordinates);

    /**
     * Resolve the GAV structure and return the {@link MavenVersionDescriptor}, i.e.
     * the leaf of the tree.
     *
     * @param groupId
     *            The groupId.
     * @param artifactId
     *            The artifactId
     * @param version
     *            The version.
     * @return The {@link MavenVersionDescriptor}.
     */
    @ResultOf
    @Cypher("MATCH" + //
            "  (repository) " + //
            "WHERE" + //
            "  id(repository)={this} " + //
            "MERGE" + //
            "  (repository)-[:CONTAINS]->(g:Maven:GroupId{name:{groupId}}) " + //
            "WITH" + //
            "  g " + //
            "MERGE" + //
            "  (g)-[:CONTAINS]->(a:Maven:ArtifactId{name:{artifactId},fqn:{groupId} + ':' + {artifactId}}) " + //
            "WITH" + //
            "  a " + //
            "MERGE" + //
            "  (a)-[:CONTAINS]->(v:Maven:Version{name:{version},fqn:{groupId} + ':' + {artifactId} + ':' + {version}}) " + //
            "RETURN" + //
            "  v")
    MavenVersionDescriptor resolveVersion(@Parameter("groupId") String groupId, @Parameter("artifactId") String artifactId,
            @Parameter("version") String version);
}
