package com.buschmais.jqassistant.core.shared.lifecycle;

/**
 * Defines the interface for instances that have a lifecycle managed by the
 * framework.
 */
public interface LifecycleAware {

    /**
     * Initialize the instance.
     *
     * Life cycle callback to perform initialization (e.g. expensive instantiaion of
     * {@link javax.xml.bind.JAXBContext}s), will be called exactly once.
     * 
     * @throws Exception
     *             If initialization fails.
     */
    default void initialize() throws Exception {
    }

    /**
     * Destroy the instance, will be called exactly once.
     * 
     * @throws Exception
     *             If destruction fails.
     */
    default void destroy() throws Exception {
    }

}
