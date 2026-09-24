package com.buschmais.jqassistant.core.runtime.impl.metrics;

import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.runtime.api.configuration.Metrics;
import com.buschmais.jqassistant.core.runtime.api.configuration.Prometheus;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider.getVersionProvider;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MeterRegistryFactoryImplTest {

    @Mock
    private Metrics metrics;

    @Mock
    private Prometheus prometheus;

    @Mock
    private Prometheus.Pushgateway pushgateway;

    @BeforeEach
    void setUp() {
        doReturn(prometheus).when(metrics)
            .prometheus();
        doReturn(pushgateway).when(prometheus)
            .pushgateway();
    }

    @ParameterizedTest
    @ValueSource(strings = { "http", "https" })
    void prometheus(String scheme) {
        doReturn(scheme).when(pushgateway)
            .scheme();
        doReturn(Optional.of("localhost:9091")).when(pushgateway)
            .address();
        doReturn("jQAssistant").when(pushgateway)
            .jobName();
        MeterRegistryFactoryImpl meterRegistryFactory = new MeterRegistryFactoryImpl(metrics);

        meterRegistryFactory.initialize();

        MeterRegistry meterRegistry = meterRegistryFactory.getMeterRegistry();
        assertThat(meterRegistry).isNotNull()
            .isInstanceOf(PrometheusMeterRegistry.class);
    }

    @Test
    void defaultMeterRegistry() {
        MeterRegistryFactoryImpl meterRegistryFactory = new MeterRegistryFactoryImpl(metrics);

        meterRegistryFactory.initialize();

        MeterRegistry meterRegistry = meterRegistryFactory.getMeterRegistry();
        assertThat(meterRegistry).isNotNull()
            .isInstanceOf(SimpleMeterRegistry.class);
    }

    @Test
    void publishVersionGaugeWithCommonTags() {
        doReturn(Map.of("build-job", "42")).when(metrics)
            .commonTags();
        MeterRegistryFactoryImpl meterRegistryFactory = new MeterRegistryFactoryImpl(metrics);

        meterRegistryFactory.initialize();

        MeterRegistry meterRegistry = meterRegistryFactory.getMeterRegistry();
        assertThat(meterRegistry.get(MeterRegistryFactoryImpl.METER_JQASSISTANT_DISTRIBUTION)
            .tag(MeterRegistryFactoryImpl.TAG_JQASSISTANT_VERSION, getVersionProvider().getVersion())
            .tag("build-job", "42")
            .gauge()
            .value()).isEqualTo(1);
    }
}
