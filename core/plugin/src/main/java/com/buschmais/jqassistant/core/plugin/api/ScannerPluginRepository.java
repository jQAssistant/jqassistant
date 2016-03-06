package com.buschmais.jqassistant.core.plugin.api;

import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.scanner.api.ScannerPlugin;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface ScannerPluginRepository {

    /**
     * Return the instances of the configured scanner plugins.
     *
     * @param scannerContext The scannerContext.
     * @param properties     The configuration properties.
     * @return The instances of the configured scanner plugins.
     * @throws PluginRepositoryException If the instances cannot be created.
     */
    Map<String, ScannerPlugin<?, ?>> getScannerPlugins(ScannerContext scannerContext, Map<String, Object> properties) throws PluginRepositoryException;

}
