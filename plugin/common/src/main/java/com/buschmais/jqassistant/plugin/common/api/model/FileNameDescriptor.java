package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Property;

public interface FileNameDescriptor extends Descriptor {

    @Indexed
    @Property("fileName")
    String getFileName();

    void setFileName(String fileName);

}
