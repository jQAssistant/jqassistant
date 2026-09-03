package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes a local {@link FileDescriptor}.
 */
@Abstract
@Label("Local")
public interface LocalFileDescriptor extends FileDescriptor {
}
