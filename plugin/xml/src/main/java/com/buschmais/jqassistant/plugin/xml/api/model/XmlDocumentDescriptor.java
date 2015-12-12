package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

@Label("Document")
public interface XmlDocumentDescriptor extends XmlDescriptor {

    @Relation("HAS_ROOT_ELEMENT")
    @Outgoing
    XmlElementDescriptor getRootElement();

    void setRootElement(XmlElementDescriptor rootElement);

    String getXmlVersion();

    void setXmlVersion(String version);

    String getCharacterEncodingScheme();

    void setCharacterEncodingScheme(String characterEncodingScheme);

    boolean isStandalone();

    void setStandalone(boolean standalone);

    boolean isXmlWellFormed();

    void setXmlWellFormed(boolean wellFormed);
}
