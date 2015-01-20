package com.buschmais.jqassistant.plugin.m2repo.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;
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
    List<ContainsArtifactDescriptor> getContainedArtifacts();

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
}
