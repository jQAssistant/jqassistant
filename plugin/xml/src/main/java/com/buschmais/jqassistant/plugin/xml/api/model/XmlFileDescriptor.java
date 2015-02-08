package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

public interface XmlFileDescriptor extends XmlDescriptor, FileDescriptor {

    @Relation("HAS_ROOT_ELEMENT")
    @Outgoing
    XmlElementDescriptor getRootElement();

    void setRootElement(XmlElementDescriptor rootElement);
}
