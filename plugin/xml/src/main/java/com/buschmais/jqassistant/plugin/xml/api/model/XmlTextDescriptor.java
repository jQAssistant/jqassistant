package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Text")
public interface XmlTextDescriptor extends XmlDescriptor, SiblingDescriptor, LineNumberDescriptor {

    String getValue();

    void setValue(String value);


}
