package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Qualifies a local {@link Descriptor}, e.g. for {@link FileDescriptor}.
 */
@Label("Local")
public interface LocalDescriptor extends Descriptor {
}
