package com.buschmais.jqassistant.core.plugin.api;

import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface ScannerPluginRepository {

    /**
     * Return the instances of the configured scanner plugins.
     * 
     * @return The instances of the configured scanner plugins.
     * @throws PluginRepositoryException
     *             If the instances cannot be created.
     */
    List<ScannerPlugin<?, ?>> getScannerPlugins() throws PluginRepositoryException;

}
