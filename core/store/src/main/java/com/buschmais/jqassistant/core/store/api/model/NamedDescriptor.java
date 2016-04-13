package com.buschmais.jqassistant.core.store.api.model;

/**
 * Defines a descriptor having a name.
 *
 * This descriptor is deprecated, use the one which is provided by `jqassistant.plugin.common`.
 */
@Deprecated
public interface NamedDescriptor extends Descriptor {

    String getName();

    void setName(String name);
}
