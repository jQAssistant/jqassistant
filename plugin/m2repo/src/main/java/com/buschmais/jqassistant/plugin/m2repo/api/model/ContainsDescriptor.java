package com.buschmais.jqassistant.plugin.m2repo.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;

/**
 * Describes a "contains" relation between {@link MavenRepositoryDescriptor} and
 * {@link RepositoryArtifactDescriptor}.
 * 
 * @author pherklotz
 */
// @Relation("CONTAINS_ARTIFACT")
public interface ContainsDescriptor extends Descriptor {

	// @Incoming
	// RepositoryArtifactDescriptor getArtifactDescriptor();
	//
	// @Property("lastModified")
	// String getLastModified();
	//
	// @Outgoing
	// MavenRepositoryDescriptor getMavenRepository();
	//
	// void setLastModified(String lastModified);
}
