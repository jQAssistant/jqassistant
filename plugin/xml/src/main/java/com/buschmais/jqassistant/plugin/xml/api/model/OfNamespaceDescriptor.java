package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.jqassistant.core.store.api.model.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface OfNamespaceDescriptor extends NamedDescriptor {

    @Relation("OF_NAMESPACE")
    XmlNamespaceDescriptor getNamespaceDeclaration();

    void setNamespaceDeclaration(XmlNamespaceDescriptor namespaceDescriptor);
}
