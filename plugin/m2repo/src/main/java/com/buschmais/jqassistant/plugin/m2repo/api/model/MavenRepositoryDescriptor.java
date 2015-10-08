package com.buschmais.jqassistant.plugin.m2repo.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.*;

/**
 * Describes a maven repository.
 * 
 * @author pherklotz
 */
@Label(value = "Repository")
public interface MavenRepositoryDescriptor extends Descriptor, MavenDescriptor {

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

    @ResultOf
    @Cypher("MATCH (repository)-[:CONTAINS_POM]->(pom:Maven:Pom:Xml) WHERE id(repository)={this} and pom.fqn={coordinates} RETURN pom")
    MavenPomXmlDescriptor findModel(@Parameter("coordinates") String coordinates);
}
