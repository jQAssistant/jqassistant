
package com.buschmais.jqassistant.scm.maven.provider;

import javax.inject.Singleton;

import com.buschmais.jqassistant.core.plugin.api.*;
import com.buschmais.jqassistant.core.plugin.impl.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.plugin.impl.PluginRepositoryImpl;

import org.apache.maven.plugin.MojoExecutionException;

@Singleton
public class PluginRepositoryProvider {

    private PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl();

    private PluginRepository pluginRepository;

    PluginRepositoryProvider() throws MojoExecutionException {
        try {
            pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader);
        } catch (PluginRepositoryException e) {
            throw new MojoExecutionException("Cannot create plugin repository.", e);
        }
    }

    public PluginRepository getPluginRepository() {
        return pluginRepository;
    }
}
