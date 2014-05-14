package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

import com.buschmais.jqassistant.core.store.api.descriptor.NamedDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Interface for value descriptors.
 * 
 * @param <V>
 *            The value type.
 */
@Label("Value")
public interface ValueDescriptor<V> extends NamedDescriptor {

    /**
     * Set the value.
     * 
     * @param value
     *            The value.
     */
    void setValue(V value);

    /**
     * Return the value.
     * 
     * @return The value.
     */
    V getValue();
}
