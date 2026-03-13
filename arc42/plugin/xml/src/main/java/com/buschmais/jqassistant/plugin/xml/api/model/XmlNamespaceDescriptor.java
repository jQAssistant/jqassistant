package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Namespace")
public interface XmlNamespaceDescriptor extends XmlDescriptor {

    String getPrefix();
    void setPrefix(String prefix);

    String getUri();
    void setUri(String namespaceURI);
}
