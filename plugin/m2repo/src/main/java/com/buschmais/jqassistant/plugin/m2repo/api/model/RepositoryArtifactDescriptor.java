package com.buschmais.jqassistant.plugin.m2repo.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;

public interface RepositoryArtifactDescriptor extends Descriptor,
		ArtifactDescriptor {

	@Incoming
	ContainsDescriptor getContainingRepository();
}
