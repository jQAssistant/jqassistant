package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

@Label("Document")
public interface JSONDocumentDescriptor extends JSONDescriptor {
    @Relation("CONTAINS")
    JSONContainer getContainer();

    void setContainer(JSONContainer jsonContainer);
}
