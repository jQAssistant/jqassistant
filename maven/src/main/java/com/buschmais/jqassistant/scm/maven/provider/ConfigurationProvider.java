package com.buschmais.jqassistant.scm.maven.provider;

import java.io.File;
import java.util.Optional;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.configuration.impl.ConfigurationLoaderImpl;

import org.codehaus.plexus.component.annotations.Component;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Provides the runtime {@link Configuration} for jQAssistant within a Maven reactor.
 * <p>
 * Declared as to allow caching the config.
 */
@Component(role = ConfigurationProvider.class, instantiationStrategy = "singleton")
public class ConfigurationProvider {

    private Configuration configuration;

    public synchronized Configuration getConfiguration(File workingDirectory, Optional<File> configurationDirectory, ConfigSource... configSources) {
        if (configuration == null) {
            ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl();
            File effectiveConfigurationDirectory = configurationDirectory.orElse(configurationLoader.getDefaultConfigurationDirectory(workingDirectory));
            this.configuration = configurationLoader.load(effectiveConfigurationDirectory, Configuration.class, configSources);
        }
        return configuration;
    }

}
