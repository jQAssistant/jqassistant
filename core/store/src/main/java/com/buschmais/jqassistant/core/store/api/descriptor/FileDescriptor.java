package com.buschmais.jqassistant.core.store.api.descriptor;

import com.buschmais.cdo.neo4j.api.annotation.Indexed;
import com.buschmais.cdo.neo4j.api.annotation.Label;
import com.buschmais.cdo.neo4j.api.annotation.Property;

/**
 * Defines a descriptor representing a file.
 */
@Label("FILE")
public interface FileDescriptor extends Descriptor {

    @Indexed
    @Property("FILENAME")
    String getFileName();

    void setFileName(String fileName);
}
