package com.buschmais.jqassistant.core.runtime.api.plugin;

import java.net.URL;
import java.util.Map;

import org.jqassistant.schema.plugin.v2.JqassistantPlugin;

/**
 * Defines the interface for plugin readers.
 */
public interface PluginConfigurationReader {

    String NAMESPACE = "http://schema.jqassistant.org/plugin/v2.4";
    String PLUGIN_RESOURCE = "META-INF/jqassistant-plugin.xml";
    String PLUGIN_SCHEMA_RESOURCE = "/META-INF/schema/jqassistant-plugin-v2.4.xsd";

    /**
     * Return the class loader used to resolve plugins.
     *
     * @return The class loader.
     */
    ClassLoader getClassLoader();

    /**
     * Return the {@link Map} of detected plugins, each one identified by a base {@link URL}.
     *
     * @return The plugins.
     */
    Map<URL, JqassistantPlugin> getPlugins();

}
