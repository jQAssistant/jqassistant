package com.buschmais.jqassistant.plugin.yaml.api.model;

import java.util.List;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ValidDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface YAMLFileDescriptor
 extends YAMLDescriptor, FileDescriptor, ValidDescriptor
{

    @Relation("CONTAINS_DOCUMENT")
    List<YAMLDocumentDescriptor> getDocuments();
}
