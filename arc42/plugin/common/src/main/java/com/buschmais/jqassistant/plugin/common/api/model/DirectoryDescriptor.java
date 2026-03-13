package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Directory")
public interface DirectoryDescriptor extends FileDescriptor, FileContainerDescriptor {
}
