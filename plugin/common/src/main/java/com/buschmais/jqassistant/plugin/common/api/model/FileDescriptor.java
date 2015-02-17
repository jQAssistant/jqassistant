package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Represents a file.
 */
@Label(value = "File", usingIndexedPropertyOf = FileNameDescriptor.class)
public interface FileDescriptor extends FileNameDescriptor {

}
