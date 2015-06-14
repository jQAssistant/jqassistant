package com.buschmais.jqassistant.plugin.yaml.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

public interface YAMLFileDescriptor extends YAMLDescriptor, FileDescriptor {

    @Relation("CONTAINS_DOCUMENT")
    List<YAMLDocumentDescriptor> getDocuments();
}
