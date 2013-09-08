package com.buschmais.jqassistant.plugin.java.impl.store.descriptor;

/**
 * Interface for value descriptors which provide a type information.
 */
public interface TypedValueDescriptor<V> extends ValueDescriptor<V> {

    TypeDescriptor getType();

    void setType(TypeDescriptor type);
}
