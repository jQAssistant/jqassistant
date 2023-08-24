package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.Arrays;
import java.util.List;

import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Defines the interface for loading runtime configuration.
 * <p>
 * The mechanism is based on Eclipse Micro Profile configuration.
 * @param <C>
 *     The configuration mapping type.
 */
public interface ConfigurationLoader<C extends Configuration> {

    /**
     * The default names of configuration files
     */
    List<String> DEFAULT_CONFIG_LOCATIONS = Arrays.asList(".jqassistant.yml", ".jqassistant.yaml", ".jqassistant");

    /**
     * The ordinal for config sources from the user home.
     */
    int ORDINAL_USER = 50;

    /**
     * The ordinal for config sources from classpath.
     */
    int ORDINAL_CLASSPATH = 80;

    /**
     * The ordinal for config sources from the working directory.
     */
    int ORDINAL_WORKING_DIRECTORY = 100;

    /**
     * Load the {@link Configuration} using the given working directory including
     * <p>
     * - yml/yaml files present in the given configuration directory
     * - system properties
     * - environment variables
     *
     * @param configSources
     *     Additional {@link ConfigSource}s to consider, e.g. from a CLI or Maven Mojo.
     * @return The {@link Configuration}.
     */
    C load(ConfigSource... configSources);
}
