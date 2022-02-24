package com.buschmais.jqassistant.core.configuration.api;

import java.io.File;

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
     * Load the {@link Configuration} using the given working directory including
     * <p>
     * - yml/yaml files present in the given configuration directory
     * - system properties
     * - environment variables
     *
     * @param configurationDirectory
     *     The configuration directory
     * @param configSources
     *     Additional {@link ConfigSource}s to consider, e.g. from a CLI or Maven Mojo.
     * @return The {@link Configuration}.
     */
    Configuration load(File configurationDirectory, ConfigSource... configSources);

    /**
     * Determines the default configuration directory relative to the given working directory.
     *
     * @param workingDirectory
     *     The working directory.
     * @return The configuration directory.
     */
    default File getDefaultConfigurationDirectory(File workingDirectory) {
        return new File(workingDirectory, DEFAULT_CONFIGURATION_DIRECTORY);
    }
}
