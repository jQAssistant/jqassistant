package com.buschmais.jqassistant.core.runtime.api.plugin;

import java.util.List;

import org.jqassistant.schema.plugin.v1.JqassistantPlugin;

/**
 * Defines the interface for plugin readers.
 */
public interface PluginConfigurationReader {

    String PLUGIN_RESOURCE = "META-INF/jqassistant-plugin.xml";
    String PLUGIN_SCHEMA_RESOURCE = "/META-INF/plugin/xsd/jqassistant-plugin-v1.10.xsd";

    /**
     * Return the class loader used to resolve plugins.
     *
     * @return The class loader.
     */
    ClassLoader     getClassLoader();

    /**
     * Return the list of detected plugins.
     *
     * @return The plugins.
     */
    List<JqassistantPlugin> getPlugins();

}
