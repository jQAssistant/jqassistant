package com.buschmais.jqassistant.core.runtime.api.bootstrap;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginClassLoader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginConfigurationReader;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginRepository;
import com.buschmais.jqassistant.core.runtime.api.plugin.PluginResolver;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginConfigurationReaderImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginRepositoryImpl;
import com.buschmais.jqassistant.core.runtime.impl.plugin.PluginResolverImpl;
import com.buschmais.jqassistant.core.shared.artifact.ArtifactProvider;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class PluginRepositoryFactory {

    public static PluginRepository getPluginRepository(Configuration configuration, ClassLoader classLoader, ArtifactProvider artifactProvider) {
        return getPluginRepository(configuration, classLoader, artifactProvider, new SimpleMeterRegistry());
    }

    public static PluginRepository getPluginRepository(Configuration configuration, ClassLoader classLoader, ArtifactProvider artifactProvider,
        MeterRegistry meterRegistry) {
        PluginResolver pluginResolver = new PluginResolverImpl(artifactProvider);
        PluginClassLoader pluginClassLoader = pluginResolver.createClassLoader(classLoader, configuration);
        PluginConfigurationReader pluginConfigurationReader = new PluginConfigurationReaderImpl(pluginClassLoader);
        PluginRepositoryImpl pluginRepository = new PluginRepositoryImpl(pluginConfigurationReader, meterRegistry);
        pluginRepository.initialize();
        return pluginRepository;
    }

}
