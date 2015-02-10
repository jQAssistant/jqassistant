package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Attribute")
public interface XmlAttributeDescriptor extends XmlDescriptor, OfNamespaceDescriptor {

    String getValue();
    void setValue(String value);

}
