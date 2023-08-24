package com.buschmais.jqassistant.core.runtime.api.configuration;

/**
 * Defines the interface for serializing an existing configuration.
 *
 * @param <C>
 *     The configurationt type.
 */
public interface ConfigurationSerializer<C> {

    /**
     * Serialize the configuration to a YAML representation.
     * @param configuration The configuration.
     * @return The YAML representation.
     */
    String toYaml(C configuration);
}
