package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.type.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.type.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Jar")
public interface JarArchiveDescriptor extends ArchiveDescriptor, ArtifactDescriptor {
}
