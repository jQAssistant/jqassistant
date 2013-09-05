package com.buschmais.jqassistant.core.analysis.api;

import com.buschmais.jqassistant.core.analysis.plugin.schema.v1.JqassistantPlugin;

import javax.xml.transform.Source;
import java.util.List;

/**
 * Defines the interface for plugin readers.
 */
public interface PluginReader {

    String PLUGIN_RESOURCE = "META-INF/jqassistant-plugin.xml";
    String PLUGIN_SCHEMA_RESOURCE = "/META-INF/xsd/jqassistant-plugin-1.0.xsd";

    /**
     * Get the list of available plugins.
     *
     * @return The list of available plugins.
     */
    List<JqassistantPlugin> readPlugins();

    /**
     * Get a list of sources providing rules.
     *
     * @param plugins The plugins.
     * @return The list of sources providing rules.
     */
    List<Source> getRuleSources(Iterable<JqassistantPlugin> plugins);
}
