package com.buschmais.jqassistant.core.pluginmanager.api;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;

import java.util.List;

/**
 * Defines the interface for plugin readers.
 */
public interface ScannerPluginManager {

    /**
     * Return the instances of the configured descriptor mappers.
     *
     * @return The instances of the configured descriptor mappers.
     * @throws com.buschmais.jqassistant.core.analysis.api.PluginReaderException If the instances cannot be created.
     */
    List<Class<?>> getDescriptorTypes() throws PluginReaderException;

    /**
     * Return the instances of the configured file scanner plugins.
     *
     * @return The instances of the configured scanner plugins.
     * @throws com.buschmais.jqassistant.core.analysis.api.PluginReaderException If the instances cannot be created.
     */
    List<FileScannerPlugin> getFileScannerPlugins() throws PluginReaderException;


    /**
     * Return the instances of the configured project scanner plugins.
     *
     * @return The instances of the configured scanner plugins.
     * @throws com.buschmais.jqassistant.core.analysis.api.PluginReaderException
     */
    List<ProjectScannerPlugin> getProjectScannerPlugins() throws PluginReaderException;
}
