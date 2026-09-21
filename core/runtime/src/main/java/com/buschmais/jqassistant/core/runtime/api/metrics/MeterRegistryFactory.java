package com.buschmais.jqassistant.core.runtime.api.metrics;

import java.io.IOException;

import com.buschmais.jqassistant.core.shared.lifecycle.LifecycleAware;

import io.micrometer.core.instrument.MeterRegistry;

public interface MeterRegistryFactory extends LifecycleAware {

    @Override
    void initialize();

    MeterRegistry getMeterRegistry();

    @Override
    void destroy() throws IOException;

}
