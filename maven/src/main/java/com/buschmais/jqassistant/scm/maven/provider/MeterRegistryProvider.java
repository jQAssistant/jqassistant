package com.buschmais.jqassistant.scm.maven.provider;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.runtime.api.metrics.MeterRegistryFactory;
import com.buschmais.jqassistant.core.runtime.impl.metrics.MeterRegistryFactoryImpl;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Disposable;

/**
 * Manages the life cycle of the {@link MeterRegistry}.
 */
@Slf4j
@Component(role = MeterRegistryProvider.class, instantiationStrategy = "singleton")
public class MeterRegistryProvider implements Disposable {

    private MeterRegistryFactory meterRegistryFactory;

    public MeterRegistry getMeterRegistry(Configuration configuration) {
        if (meterRegistryFactory == null) {
            this.meterRegistryFactory = new MeterRegistryFactoryImpl(configuration.metrics());
            this.meterRegistryFactory.initialize();
        }
        return this.meterRegistryFactory.getMeterRegistry();
    }

    @Override
    public void dispose() {
        if (meterRegistryFactory != null) {
            meterRegistryFactory.destroy();
        }
    }
}
