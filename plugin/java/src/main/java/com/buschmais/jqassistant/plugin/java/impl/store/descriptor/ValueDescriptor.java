package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.Descriptor;

/**
 * Interface for value descriptors.
 *
 * @param <V> The value type.
 */
public interface ValueDescriptor<V> extends Descriptor {

    /**
     * Set the name.
     *
     * @return The name.
     */
    String getName();

    /**
     * Return the name.
     *
     * @param name The name.
     */
    void setName(String name);

    /**
     * Set the value.
     *
     * @param value The value.
     */
    void setValue(V value);

    /**
     * Return the value.
     *
     * @return The value.
     */
    V getValue();

}
