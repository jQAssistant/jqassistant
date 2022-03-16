package com.buschmais.jqassistant.core.configuration.api;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.plugin.api.configuration.Plugin;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.store.api.configuration.Store;

import io.smallrye.config.ConfigMapping;

/**
 * Represents the runtime configuration for jQAssistant.
 */
@ConfigMapping(prefix = Configuration.PREFIX)
public interface Configuration {

    String PREFIX = "jqassistant";

    /**
     * The plugins to provision.
     *
     * @return The {@link List} of plugins.
     */
    List<Plugin> plugins();

    /**
     * The {@link Store} configuration.
     *
     * @return The {@link Store} configuration.
     */
    Store store();

    /**
     * The {@link Scan} configuration.
     *
     * @return The {@link Scan} configuration.
     */
    Scan scan();

    /**
     * The {@link Analyze} configuration.
     *
     * @return The {@link Analyze} configuration.
     */
    Analyze analyze();
}
