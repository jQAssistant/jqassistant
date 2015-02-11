package com.buschmais.jqassistant.plugin.common.test.scanner;

import com.buschmais.jqassistant.core.store.api.model.DirectoryDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Dependent")
public interface DependentDirectoryDescriptor extends DirectoryDescriptor {

    void setValue(String value);

    String getValue();

}
