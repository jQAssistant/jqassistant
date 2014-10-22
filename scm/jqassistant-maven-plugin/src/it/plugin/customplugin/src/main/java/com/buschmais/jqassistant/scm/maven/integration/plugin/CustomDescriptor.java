package com.buschmais.jqassistant.scm.maven.integration.plugin;

import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Custom")
public interface CustomDescriptor extends ClassFileDescriptor {

    String getValue();

    void setValue(String value);

}
