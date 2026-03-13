package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Descriptor for a contributor entry defined in a pom.xml.
 * 
 * @see org.apache.maven.model.Contributor
 */
@Label("Contributor")
public interface MavenContributorDescriptor extends MavenProjectParticipantDescriptor {
}
