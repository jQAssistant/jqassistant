package com.buschmais.jqassistant.scm.maven.provider;

import javax.inject.Singleton;

import com.buschmais.jqassistant.core.shared.configuration.ConfigurationFileLoader;

import lombok.Getter;

/**
 * Singleton component providing the {@link ConfigurationFileLoader} for a reactor.
 */
@Getter
@Singleton
public class ConfigurationFileLoaderProvider {

    private final ConfigurationFileLoader configurationFileLoader = new ConfigurationFileLoader();

}
