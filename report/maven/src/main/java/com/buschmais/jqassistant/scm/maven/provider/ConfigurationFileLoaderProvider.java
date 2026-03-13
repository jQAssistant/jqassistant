package com.buschmais.jqassistant.scm.maven.provider;

import com.buschmais.jqassistant.core.shared.configuration.ConfigurationFileLoader;

import lombok.Getter;
import org.codehaus.plexus.component.annotations.Component;

/**
 * Singleton component providing the {@link ConfigurationFileLoader} for a reactor.
 */
@Getter
@Component(role = ConfigurationFileLoaderProvider.class, instantiationStrategy = "singleton")
public class ConfigurationFileLoaderProvider {

    private final ConfigurationFileLoader configurationFileLoader = new ConfigurationFileLoader();

}
