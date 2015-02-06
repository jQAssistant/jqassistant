package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.jqassistant.plugin.common.api.model.XmlDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Attribute")
public interface XmlAttributeDescriptor extends XmlDescriptor, NamespaceDescriptor {

    String getValue();
    void setValue(String value);

}
