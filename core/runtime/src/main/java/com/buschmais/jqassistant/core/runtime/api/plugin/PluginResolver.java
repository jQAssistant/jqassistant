package com.buschmais.jqassistant.core.runtime.api.plugin;

import java.util.List;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;

import org.eclipse.aether.repository.RemoteRepository;

/**
 * Defines the interface for a plugin resolver that takes a list of required plugins from the configuration, resolves the artifacts and provides the {@link PluginClassLoader}.
 */
public interface PluginResolver {

    /**
     * Resolve the required plugins.
     *
     * @param parent
     *     The paren {@link ClassLoader}.
     * @param configuration
     *     The {@link Configuration}.
     * @return The {@link PluginClassLoader}.
     */
    PluginClassLoader createClassLoader(ClassLoader parent, Configuration configuration);

    /**
     * Return The list of configured remote repositories.
     *
     * @return The list of configured remote repositories.
     */
    List<RemoteRepository> getRepositories();
}
