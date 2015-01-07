package com.buschmais.jqassistant.plugin.m2repo.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenDescriptor;

public interface RepositoryArtifactDescriptor extends MavenDescriptor, ArtifactDescriptor {

    MavenRepositoryDescriptor getContainingRepository();
}
