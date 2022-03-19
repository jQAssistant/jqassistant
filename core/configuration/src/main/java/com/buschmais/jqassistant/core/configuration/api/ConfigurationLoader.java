package com.buschmais.jqassistant.core.configuration.api;

import java.util.Arrays;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Defines the interface for loading runtime configuration.
 * <p>
 * The mechanism is based on Eclipse Micro Profile configuration.
 */
public interface ConfigurationLoader {

    /**
     * The path to the default configuration repository, relative to the working directory.
     */
    String DEFAULT_CONFIGURATION_DIRECTORY = ".jqassistant";

    /**
     * The default names of configuration files
     */
    List<String> DEFAULT_CONFIGURATION_FILES = Arrays.asList("jqassistant.yml", "jqassistant.yaml");

    /**
     * Load the {@link Configuration} using the given working directory including
     * <p>
     * - yml/yaml files present in the given configuration directory
     * - system properties
     * - environment variables
     *
     * @param <C>
     *     The configuration mapping type.
     * @param configurationMapping
     *     The class to be returned as configuration mapping.
     * @param configSources
     *     Additional {@link ConfigSource}s to consider, e.g. from a CLI or Maven Mojo.
     * @return The {@link Configuration}.
     */
    <C extends Configuration> C load(Class<C> configurationMapping, ConfigSource... configSources);

}
