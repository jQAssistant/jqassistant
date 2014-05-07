package com.buschmais.jqassistant.core.pluginrepository.api;

import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;

/**
 * Defines the interface for the scanner plugin repository.
 */
public interface ScannerPluginRepository extends PluginRepository {

    /**
     * Return the instances of the configured descriptor mappers.
     * 
     * @return The instances of the configured descriptor mappers.
     * @throws PluginRepositoryException
     *             If the instances cannot be created.
     */
    List<Class<?>> getDescriptorTypes() throws PluginRepositoryException;

    /**
     * Return the instances of the configured file scanner plugins.
     * 
     * @return The instances of the configured scanner plugins.
     * @throws PluginRepositoryException
     *             If the instances cannot be created.
     */
    List<FileScannerPlugin> getFileScannerPlugins() throws PluginRepositoryException;

    /**
     * Return the instances of the configured project scanner plugins.
     * 
     * @return The instances of the configured scanner plugins.
     * @throws PluginRepositoryException
     */
    List<ProjectScannerPlugin> getProjectScannerPlugins() throws PluginRepositoryException;
}
