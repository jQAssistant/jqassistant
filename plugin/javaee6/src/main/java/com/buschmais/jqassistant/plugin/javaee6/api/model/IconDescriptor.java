package com.buschmais.jqassistant.plugin.javaee6.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Icon")
public interface IconDescriptor extends Descriptor, LangDescriptor {

    String getLargeIcon();

    void setLargeIcon(String value);

    String getSmallIcon();

    void setSmallIcon(String value);
}
