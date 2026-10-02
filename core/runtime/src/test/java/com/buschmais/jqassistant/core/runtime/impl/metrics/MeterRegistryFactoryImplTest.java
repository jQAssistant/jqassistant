package com.buschmais.jqassistant.core.runtime.impl.metrics;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.runtime.api.configuration.Metrics;
import com.buschmais.jqassistant.core.runtime.api.configuration.Prometheus;
import com.buschmais.jqassistant.core.runtime.api.configuration.Prometheus.Pushgateway;
import com.buschmais.jqassistant.core.runtime.api.metrics.MeterRegistryFactory;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider.getVersionProvider;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class MeterRegistryFactoryImplTest {

    @Mock
    private Metrics metrics;

    @Mock
    private Prometheus prometheus;

    @Mock
    private Pushgateway pushgateway;

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
        assertThat(meterRegistry.get(MeterRegistryFactory.METER_JQASSISTANT_DISTRIBUTION)
            .tag(MeterRegistryFactory.TAG_JQASSISTANT_VERSION, getVersionProvider().getVersion())
            .tag("build-job", "42")
            .gauge()
            .value()).isEqualTo(1);
    }

    @ExtendWith(MockitoExtension.class)
    abstract class AbstractPushgatewayTest {

        protected WireMockServer wireMockServer;

        @BeforeEach
        void setUp() {
            wireMockServer = new WireMockServer(0);
            wireMockServer.start();
            wireMockServer.addStubMapping(put(urlEqualTo("/metrics/job/job-42")).willReturn(aResponse().withStatus(200))
                .build());
            doReturn("http").when(pushgateway)
                .scheme();
            doReturn(Optional.of("localhost:" + wireMockServer.port())).when(pushgateway)
                .address();
            doReturn("job-42").when(pushgateway)
                .jobName();
        }

        @Test
        final void push() throws IOException {
            // given
            configure();
            MeterRegistryFactoryImpl meterRegistryFactory = new MeterRegistryFactoryImpl(metrics);
            meterRegistryFactory.initialize();

            // when
            meterRegistryFactory.destroy();

            // then
            verify();
        }

        protected abstract void configure();

        protected abstract void verify();

        @AfterEach
        void tearDown() {
            if (wireMockServer != null) {
                wireMockServer.stop();
            }
        }
    }

    @Nested
    class PushWithoutAuthTest extends AbstractPushgatewayTest {

        @Override
        protected void configure() {
        }

        @Override
        protected void verify() {
            wireMockServer.verify(1, putRequestedFor(urlEqualTo("/metrics/job/job-42")).withRequestBody(matching(".*jqassistant_distribution.*"))
                .withoutHeader("Authorization"));
        }
    }

    @Nested
    class PushWithBasicAuthTest extends AbstractPushgatewayTest {

        @Override
        protected void configure() {
            doReturn(Optional.of("foo")).when(pushgateway)
                .username();
            doReturn(Optional.of("bar")).when(pushgateway)
                .password();
        }

        @Override
        protected void verify() {
            wireMockServer.verify(1, putRequestedFor(urlEqualTo("/metrics/job/job-42")).withRequestBody(matching(".*jqassistant_distribution.*"))
                .withBasicAuth(new BasicCredentials("foo", "bar")));
        }
    }

    @Nested
    class PushWithTokenAuthTest extends AbstractPushgatewayTest {

        @Override
        protected void configure() {
            doReturn(Optional.of("test-token")).when(pushgateway)
                .bearerToken();
        }

        @Override
        protected void verify() {
            wireMockServer.verify(1, putRequestedFor(urlEqualTo("/metrics/job/job-42")).withRequestBody(matching(".*jqassistant_distribution.*"))
                .withHeader("Authorization", matching("Bearer test-token")));
        }
    }

}
