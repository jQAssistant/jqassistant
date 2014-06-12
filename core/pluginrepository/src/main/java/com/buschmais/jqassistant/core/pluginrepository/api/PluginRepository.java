package com.buschmais.jqassistant.core.pluginrepository.api;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;

/**
 * Defines the interface for plugin readers.
 */
public interface PluginRepository {

    String PLUGIN_RESOURCE = "META-INF/jqassistant-plugin.xml";
    String PLUGIN_SCHEMA_RESOURCE = "/META-INF/xsd/jqassistant-plugin-1.0.xsd";

    List<JqassistantPlugin> getPlugins();

}
