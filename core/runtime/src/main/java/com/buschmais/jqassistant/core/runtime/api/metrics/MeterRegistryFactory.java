package com.buschmais.jqassistant.core.runtime.api.metrics;

import java.io.IOException;

import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Defines the factory for the Micrometer {@link MeterRegistry}.
 */
public interface MeterRegistryFactory extends LifecycleAware {

    @Override
    void initialize();

    MeterRegistry getMeterRegistry();

    @Override
    void destroy() throws IOException;

}
