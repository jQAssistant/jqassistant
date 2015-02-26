package com.buschmais.jqassistant.plugin.java.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ArchiveDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Jar")
public interface JarArchiveDescriptor extends JavaArtifactFileDescriptor, ArchiveDescriptor {
}
