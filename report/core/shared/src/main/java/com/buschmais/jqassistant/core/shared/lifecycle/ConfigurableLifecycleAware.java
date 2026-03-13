package com.buschmais.jqassistant.core.shared.lifecycle;

/**
 * Defines the interface for {@link LifecycleAware} instances that are
 * configurable.
 * 
 * @param <CONFIGURATION>
 *            The configuration type.
 */
public interface ConfigurableLifecycleAware<CONFIGURATION> extends LifecycleAware {

    /**
     * Configure the instance.
     *
     * This method is always called at least once after {@link #initialize()} and
     * allows re-configuring a an instance at runtime (e.g. in a Maven multi-module
     * build process).
     *
     * @param configuration
     *            The configuration.
     * @throws Exception
     *             If configuration fails.
     */
    default void configure(CONFIGURATION configuration) throws Exception {
    }

}
