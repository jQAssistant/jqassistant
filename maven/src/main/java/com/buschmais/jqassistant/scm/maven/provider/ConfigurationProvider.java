package com.buschmais.jqassistant.scm.maven.provider;

import java.io.File;
import java.util.Optional;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.configuration.impl.ConfigurationLoaderImpl;
import com.buschmais.jqassistant.scm.maven.configuration.MavenConfiguration;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Provides the runtime {@link Configuration} for jQAssistant within a Maven reactor.
 * <p>
 * Declared as singleton to allow caching the {@link ConfigurationLoader} instance.
 */
@Component(role = ConfigurationProvider.class, instantiationStrategy = "singleton")
public class ConfigurationProvider {

    /**
     * Cached {@link ConfigurationLoader} instance.
     */
    private ConfigurationLoader configurationLoader;

    /**
     * Return the Configuration.
     *
     * @param executionRoot
     *     The Session execution root.
     * @param configurationDirectory
     *     The optional configuration directory.
     * @param configSources
     *     Additional {@link ConfigSource}s.
     * @return The {@link Configuration}.
     */
    public synchronized MavenConfiguration getConfiguration(File executionRoot, Optional<File> configurationDirectory, ConfigSource... configSources) {
        if (configurationLoader == null) {
            File effectiveConfigurationDirectory = configurationDirectory.orElse(ConfigurationLoader.getDefaultConfigurationDirectory(executionRoot));
            configurationLoader = new ConfigurationLoaderImpl(effectiveConfigurationDirectory);
        }
        return configurationLoader.load(MavenConfiguration.class, configSources);
    }

}
