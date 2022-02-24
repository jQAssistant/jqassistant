package com.buschmais.jqassistant.core.plugin.api;

import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;

/**
 * Defines the interface for a plugin resolver that takes a list of required plugins from the configuration, resolves the artifacts and provides the {@link PluginClassLoader}.
 */
public interface PluginResolver {

    /**
     * Resolve the required plugins.
     *
     * @param parent
     *     The paren {@link ClassLoader}.
     * @param plugins
     *     The required plugins.
     * @return The {@link PluginClassLoader}.
     */
    PluginClassLoader createClassLoader(ClassLoader parent, List<Plugin> plugins);
}
