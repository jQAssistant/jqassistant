package com.buschmais.jqassistant.plugin.yaml2.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Document")
public interface YMLDocumentDescriptor extends YMLDescriptor {

    @Relation("HAS_SEQUENCE")
    List<YMLSequenceDescriptor> getSequences();

}
