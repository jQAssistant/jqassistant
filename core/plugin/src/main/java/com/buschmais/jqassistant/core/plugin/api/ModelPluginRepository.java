package com.buschmais.jqassistant.core.plugin.api;

import java.util.List;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface ModelPluginRepository {

    /**
     * Return the instances of the configured descriptor mappers.
     *
     * @return The instances of the configured descriptor mappers.
     * @throws com.buschmais.jqassistant.core.plugin.api.PluginRepositoryException
     *             If the instances cannot be created.
     */
    List<Class<?>> getDescriptorTypes() throws PluginRepositoryException;
}
