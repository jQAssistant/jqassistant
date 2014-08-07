package com.buschmais.jqassistant.core.store.api.type;

import com.buschmais.xo.neo4j.api.annotation.Indexed;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Property;

/**
 * Represents a file.
 */
@Label("File")
public interface FileDescriptor extends Descriptor {

    @Indexed
    @Property("fileName")
    String getFileName();

    void setFileName(String fileName);
}
