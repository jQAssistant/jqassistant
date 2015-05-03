package com.buschmais.jqassistant.plugin.m2repo.api.model;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.*;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

/**
 * Describes a maven repository.
 * 
 * @author pherklotz
 */
@Label(value = "Repository")
public interface MavenRepositoryDescriptor extends Descriptor, MavenDescriptor {

    /**
     * A list of contained artifacts.
     */
    @Outgoing
    @ContainsArtifact
    List<RepositoryArtifactDescriptor> getContainedArtifacts();

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
     * The last scan date.
     * 
     * @return the last scan date.
     */
    @Property("lastScanDate")
    long getLastScanDate();

    /**
     * Set the last scan date.
     * 
     * @param scanDate
     *            the last scan date.
     */
    void setLastScanDate(long scanDate);

    @ResultOf
    @Cypher("MATCH (r:Maven:Repository)-[:CONTAINS_ARTIFACT]->(a:RepositoryArtifact) WHERE id(r)={this} and a.mavenCoordinates={coordinates} RETURN a")
    RepositoryArtifactDescriptor getArtifact(@Parameter("coordinates") String coordinates);

    @ResultOf
    @Cypher("MATCH (r:Maven:Repository)-[:CONTAINS_ARTIFACT]->(a:RepositoryArtifact) WHERE id(r)={this} and a.mavenCoordinates={coordinates} and a.lastModified={lastModified} RETURN a")
    RepositoryArtifactDescriptor getSnapshotArtifact(@Parameter("coordinates") String coordinates, @Parameter("lastModified") long lastModified);

    @ResultOf
    @Cypher("MATCH (r:Maven:Repository)-[:CONTAINS_ARTIFACT]->(a:RepositoryArtifact) WHERE id(r)={this} and a.mavenCoordinates={coordinates} AND a.lastModified<>{lastModified} RETURN a ORDER BY a.lastModified DESC LIMIT 1")
    RepositoryArtifactDescriptor getLastSnapshot(@Parameter("coordinates") String coordinates, @Parameter("lastModified") long lastModified);

    @Relation("CONTAINS_ARTIFACT")
    @Retention(RetentionPolicy.RUNTIME)
    public @interface ContainsArtifact {
    }
}
