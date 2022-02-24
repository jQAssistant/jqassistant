package com.buschmais.jqassistant.core.configuration.api;

import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;

import io.smallrye.config.ConfigMapping;

/**
 * Represents the runtime configuration for jQAssistant.
 */
@ConfigMapping(prefix = "jqassistant")
public interface Configuration {

    /**
     * The plugins to provision.
     *
     * @return The {@link List} of plugins.
     */
    List<Plugin> plugins();

}
