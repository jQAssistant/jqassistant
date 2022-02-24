package com.buschmais.jqassistant.scm.maven.provider;

import java.io.File;
import java.util.Optional;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.configuration.api.ConfigurationLoader;
import com.buschmais.jqassistant.core.configuration.impl.ConfigurationLoaderImpl;

import org.codehaus.plexus.component.annotations.Component;

/**
 * Provides the runtime {@link Configuration} for jQAssistant within a Maven reactor.
 *
 * Declared as to allow caching the config.
 */
@Component(role = ConfigurationProvider.class, instantiationStrategy = "singleton")
public class ConfigurationProvider {

    private Configuration configuration;

    public synchronized Configuration getConfiguration(File workingDirectory, Optional<File> configurationDirectory) {
        if (configuration == null) {
            ConfigurationLoader configurationLoader = new ConfigurationLoaderImpl();
            this.configuration = configurationLoader.load(
                configurationDirectory.orElse(configurationLoader.getDefaultConfigurationDirectory(workingDirectory)));
        }
        return configuration;
    }

}
