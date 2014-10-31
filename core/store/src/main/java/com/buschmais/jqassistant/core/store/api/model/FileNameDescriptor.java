package com.buschmais.jqassistant.core.store.api.model;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Property;

public interface FileNameDescriptor extends Descriptor {

    @Indexed
    @Property("fileName")
    String getFileName();

    void setFileName(String fileName);

}
