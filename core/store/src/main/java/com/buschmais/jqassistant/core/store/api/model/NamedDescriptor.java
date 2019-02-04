package com.buschmais.jqassistant.core.store.api.model;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;
import com.buschmais.xo.neo4j.api.annotation.Indexed;

/**
 * Defines a descriptor having a name.
 *
 * This descriptor is deprecated, use the one which is provided by
 * `jqassistant.plugin.common`.
 */
@Deprecated
@ToBeRemovedInVersion(major = 1, minor = 8)
public interface NamedDescriptor extends Descriptor {

    @Indexed
    String getName();

    void setName(String name);
}
