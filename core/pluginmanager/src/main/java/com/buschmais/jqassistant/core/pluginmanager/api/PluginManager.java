package com.buschmais.jqassistant.core.pluginmanager.api;

import java.util.List;

import javax.xml.transform.Source;

import com.buschmais.jqassistant.core.analysis.api.PluginReaderException;
import com.buschmais.jqassistant.core.scanner.api.FileScannerPlugin;
import com.buschmais.jqassistant.core.store.impl.dao.mapper.DescriptorMapper;

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
	 * @throws com.buschmais.jqassistant.core.analysis.api.PluginReaderException
	 *             If the instances cannot be created.
	 */
	List<DescriptorMapper<?>> getDescriptorMappers() throws PluginReaderException;

	/**
	 * Return the instances of the configured scanner plugins.
	 * 
	 * @return The instances of the configured scanner plugins.
	 * @throws PluginReaderException
	 *             If the instances cannot be created.
	 */
	List<FileScannerPlugin<?>> getScannerPlugins() throws PluginReaderException;
}
