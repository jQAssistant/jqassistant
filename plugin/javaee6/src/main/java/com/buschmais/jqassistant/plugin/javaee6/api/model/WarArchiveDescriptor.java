package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.core.store.api.model.ArchiveDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a WAR archive.
 */
@Label("War")
public interface WarArchiveDescriptor extends ArchiveDescriptor, ArtifactDescriptor {
}
