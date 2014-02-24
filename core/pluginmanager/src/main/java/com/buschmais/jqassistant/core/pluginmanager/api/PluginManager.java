package com.buschmais.jqassistant.core.pluginmanager.api;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.scanner.api.ProjectScannerPlugin;
import com.buschmais.jqassistant.core.store.api.Store;

import javax.xml.transform.Source;
import java.util.List;
import java.util.Properties;

/**
 * Defines the interface for plugin readers.
 */
public interface PluginManager {

    String PLUGIN_RESOURCE = "META-INF/jqassistant-plugin.xml";
    String PLUGIN_SCHEMA_RESOURCE = "/META-INF/xsd/jqassistant-plugin-1.0.xsd";

    /**
     * Get a list of sources providing rules.
     *
     * @return The list of sources providing rules.
     */
    List<Source> getRuleSources();

    /**
     * Return the instances of the configured descriptor mappers.
     *
     * @return The instances of the configured descriptor mappers.
     * @throws com.buschmais.jqassistant.core.analysis.api.PluginReaderException If the instances cannot be created.
     */
    List<Class<?>> getDescriptorTypes() throws PluginReaderException;

    /**
     * Return the instances of the configured scanner plugins.
     *
     * @param store      The {@link Store} instance.
     * @param properties The {@link Properties} to be used for plugin initialization.
     * @return The instances of the configured scanner plugins.
     * @throws PluginReaderException If the instances cannot be created.
     */
    List<FileScannerPlugin<?>> getScannerPlugins(Store store, Properties properties) throws PluginReaderException;


	/**
	 * @return
	 * @throws PluginReaderException
	 */
	List<ProjectScannerPlugin<?>> getProjectScannerPlugins() throws PluginReaderException;
}
