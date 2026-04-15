package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Denotes a "main" artifact produced created by a Maven module (e.g. from
 * sources located in src/main/java".
 */
@Label("Main")
public interface MavenMainArtifactDescriptor extends MavenArtifactFileDescriptor {
}
