package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Icon")
public interface IconDescriptor extends LangDescriptor, WebDescriptor {
    String getLargeIcon();

    void setLargeIcon(String value);

    String getSmallIcon();

    void setSmallIcon(String value);
}
