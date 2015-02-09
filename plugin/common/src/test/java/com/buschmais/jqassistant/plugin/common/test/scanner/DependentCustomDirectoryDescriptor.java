package com.buschmais.jqassistant.plugin.common.test.scanner;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Dependent")
public interface DependentCustomDirectoryDescriptor extends CustomDirectoryDescriptor {

    void setValue(String value);

    String getValue();

}
