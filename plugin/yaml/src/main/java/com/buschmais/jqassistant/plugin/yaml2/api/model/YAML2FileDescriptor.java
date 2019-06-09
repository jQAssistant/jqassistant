package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

// todo Die Datei ist immer valide, aber nicht das Dokument ValidDescriptor
public interface YAML2FileDescriptor
extends YAML2Descriptor, FileDescriptor, Descriptor {

    @Relation("CONTAINS_DOCUMENT")
    List<YAML2DocumentDescriptor> getDocuments();
}
