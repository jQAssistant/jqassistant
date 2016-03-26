package com.buschmais.jqassistant.plugins.json.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Property;
import com.buschmais.xo.neo4j.api.annotation.Relation;

import java.util.List;

public interface JSONFileDescriptor extends JSONDescriptor, FileDescriptor {

    @Relation("CONTAINS_DOCUMENT")
    JSONDocumentDescriptor getDocument();

    void setDocument(JSONDocumentDescriptor document);

    void setParsed(boolean parsable);

    @Property("parsed")
    boolean getParsed();
}
