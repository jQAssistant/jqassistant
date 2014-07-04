package com.buschmais.jqassistant.plugin.java.api.model;

import java.util.Set;

import com.buschmais.jqassistant.core.store.api.descriptor.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("JarArchive")
public interface JarArchiveDescriptor extends FileDescriptor {

	@Relation("CONTAINS")
	@Outgoing
	Set<FileDescriptor> getContents();
	
}
