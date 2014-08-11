package com.buschmais.jqassistant.scm.maven;

/**
 * Defines the valid values for the life cycle of the store.
 */
public enum StoreLifecycle {
    /**
     * Per module
     */
    MODULE,

    /**
     * Per reactor, i.e. cached.
     */
    REACTOR;

}
