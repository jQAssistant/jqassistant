package com.buschmais.jqassistant.core.store.api.descriptor;

import com.buschmais.xo.neo4j.api.annotation.Label;

@Label("Directory")
public interface DirectoryDescriptor {

    String getDirectoryName();

    void setDirectoryName(String directoryName);
}
