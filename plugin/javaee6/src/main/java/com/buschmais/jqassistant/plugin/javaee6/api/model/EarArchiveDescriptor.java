package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a EAR archive.
 */
@Label("Ear")
public interface EarArchiveDescriptor extends ArchiveDescriptor, ArtifactDescriptor {
}
