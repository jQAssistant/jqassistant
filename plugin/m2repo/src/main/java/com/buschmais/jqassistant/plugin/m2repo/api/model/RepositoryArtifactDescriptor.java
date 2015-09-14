package com.buschmais.jqassistant.plugin.m2repo.api.model;

import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor.ContainsPom;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenPomXmlDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

/**
 * Represents an artifact in a maven repository.
 * 
 * @author pherklotz
 */
@Label("RepositoryArtifact")
public interface RepositoryArtifactDescriptor extends MavenPomXmlDescriptor {

    /**
     * The containing repository.
     * 
     * @return the containing repository.
     */
    @Incoming
    @ContainsPom
    MavenRepositoryDescriptor getContainingRepository();

    void setContainingRepository(MavenRepositoryDescriptor containsArtifactDescriptor);

    /**
     * The last modified date as String.
     * 
     * @return the last modified date as String.
     */
    @Property("lastModified")
    long getLastModified();

    void setLastModified(long lastModified);

    /**
     * The maven coordinates
     * <groupId>:<artifactId>:<type>:(<classifier>:)<version>.
     * 
     * @return the maven coordinates.
     */
    @Property("mavenCoordinates")
    String getMavenCoordinates();

    void setMavenCoordinates(String mavenCoordinates);

    /**
     * The last version of that SNAPSHOT artifact, if existing.
     * 
     * @return The last version of that SNAPSHOT artifact, if existing.
     */
    @Relation("HAS_PREDECESSOR")
    RepositoryArtifactDescriptor getPredecessorArtifact();

    void setPredecessorArtifact(RepositoryArtifactDescriptor predecessorArtifact);

}
