package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Jar")
public interface JarArchiveDescriptor extends JavaArtifactDescriptor, ArchiveDescriptor {
}
