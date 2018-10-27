package com.buschmais.jqassistant.core.shared.lifecycle;

/**
 * Defines the interface for {@link LifecycleAware} instances that are
 * configurable and using a context.
 * 
 * @param <CONTEXT>
 *            The context type.
 * @param <CONFIGURATION>
 *            The configuration type.
 */
public interface ContextualConfigurableLifecycleAware<CONTEXT, CONFIGURATION> extends LifecycleAware {

    /**
     * Configure the instance.
     *
     * This method is always called at least once after {@link #initialize()} and
     * allows re-configuring a an instance at runtime (e.g. in a Maven multi-module
     * build process).
     *
     * @param context
     *            The context.
     * @param configuration
     *            The configuration.
     * @throws Exception
     *             If configuration fails.
     */
    default void configure(CONTEXT context, CONFIGURATION configuration) throws Exception {
    }

}
