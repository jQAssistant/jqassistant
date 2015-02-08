package com.buschmais.jqassistant.plugin.xml.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Characters")
public interface XmlCharactersDescriptor extends XmlDescriptor {

    String getData();

    void setData(String data);

    boolean isCData();

    void setCData(boolean cData);

}
