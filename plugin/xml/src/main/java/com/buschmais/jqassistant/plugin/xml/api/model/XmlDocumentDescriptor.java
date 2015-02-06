package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.XmlDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("Document")
public interface XmlDocumentDescriptor extends XmlDescriptor, FileDescriptor {

    @Relation("HAS_ROOT_ELEMENT")
    @Outgoing
    XmlElementDescriptor getRootElement();
    void setRootElement(XmlElementDescriptor rootElement);
}
