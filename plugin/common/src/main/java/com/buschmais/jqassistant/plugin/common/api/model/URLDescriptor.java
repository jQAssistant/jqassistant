package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Qualifies a URL {@link Descriptor}, e.g. for {@link FileDescriptor}.
 */
@Label("URL")
public interface URLDescriptor extends Descriptor {
}
