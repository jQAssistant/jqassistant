package com.buschmais.jqassistant.plugin.common.api.model;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.xo.api.annotation.Abstract;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Qualifies a local item, e.g. a {@link FileDescriptor}.
 */
@Abstract
@Label("Local")
public interface LocalDescriptor extends Descriptor {
}
