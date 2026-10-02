package com.buschmais.jqassistant.core.runtime.api.metrics;

import java.io.IOException;

import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * Defines the factory for the Micrometer {@link MeterRegistry}.
 */
public interface MeterRegistryFactory extends LifecycleAware {

    String METER_JQASSISTANT_DISTRIBUTION = "jqassistant_distribution";
    String TAG_JQASSISTANT_VERSION = "version";

    @Override
    void initialize();

    MeterRegistry getMeterRegistry();

    @Override
    void destroy() throws IOException;

}
