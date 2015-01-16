package com.buschmais.jqassistant.plugin.maven3.api.model;

import com.buschmais.jqassistant.core.store.api.model.FullQualifiedNameDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Basic descriptor for pom.xml files.
 * 
 * @author ronald.kunzmann@buschmais.com
 */
@Label("Pom")
public interface MavenPomDescriptor extends MavenArtifactDescriptor, FullQualifiedNameDescriptor {
}
