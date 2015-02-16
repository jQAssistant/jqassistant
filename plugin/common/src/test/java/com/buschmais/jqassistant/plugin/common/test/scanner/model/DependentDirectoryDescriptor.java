package com.buschmais.jqassistant.plugin.common.test.scanner.model;

import com.buschmais.jqassistant.plugin.common.api.model.DirectoryDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Dependent")
public interface DependentDirectoryDescriptor extends DirectoryDescriptor {

    void setValue(String value);

    String getValue();

}
