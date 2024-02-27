package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.List;

import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.scanner.api.configuration.Scan;
import com.buschmais.jqassistant.core.shared.annotation.Description;
import com.buschmais.jqassistant.core.shared.configuration.Plugin;
import com.buschmais.jqassistant.core.store.api.configuration.Store;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Represents the runtime configuration for jQAssistant.
 */
@ConfigMapping(prefix = Configuration.PREFIX)
public interface Configuration {

    String PREFIX = "jqassistant";

    String SKIP = "skip";

    @WithDefault("false")
    @Description("Skip execution of jQAssistant tasks/goals.")
    boolean skip();

    /**
     * The default plugins to provision, to be used by distributions.
     *
     * @return The {@link List} of plugins.
     */
    List<Plugin> defaultPlugins();

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

    /**
     * The {@link Server} configuration.
     *
     * @return The {@link Server} configuration.
     */
    Server server();
}
