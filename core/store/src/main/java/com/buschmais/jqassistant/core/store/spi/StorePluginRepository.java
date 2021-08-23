package com.buschmais.jqassistant.core.store.spi;

import java.util.List;

import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface StorePluginRepository extends LifecycleAware  {

    /**
     * Return the instances of the configured descriptor mappers.
     *
     * @return The instances of the configured descriptor mappers.
     */
    List<Class<?>> getDescriptorTypes();

    List<Class<?>> getProcedureTypes();

    List<Class<?>> getFunctionTypes();

    @Override
    default void initialize() {
    }

    @Override
    default void destroy() {
    }
}


