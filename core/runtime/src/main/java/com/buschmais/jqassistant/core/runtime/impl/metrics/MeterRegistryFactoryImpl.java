package com.buschmais.jqassistant.core.runtime.impl.metrics;

import java.io.IOException;
import java.util.List;

import com.buschmais.jqassistant.core.runtime.api.configuration.Metrics;
import com.buschmais.jqassistant.core.runtime.api.configuration.Prometheus;
import com.buschmais.jqassistant.core.runtime.api.metrics.MeterRegistryFactory;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import io.micrometer.prometheusmetrics.PrometheusConfig;
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry;
import io.prometheus.metrics.exporter.pushgateway.PushGateway;
import io.prometheus.metrics.exporter.pushgateway.Scheme;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import static com.buschmais.jqassistant.core.runtime.api.bootstrap.VersionProvider.getVersionProvider;
import static io.micrometer.core.instrument.Metrics.addRegistry;
import static java.util.stream.Collectors.toList;

@Slf4j
@RequiredArgsConstructor
public class MeterRegistryFactoryImpl implements MeterRegistryFactory {

    public static final String METER_JQASSISTANT_DISTRIBUTION = "jqassistant-distribution";

    public static final String TAG_JQASSISTANT_VERSION = "version";

    private final Metrics metrics;

    private PushGateway pushGateway;

    private MeterRegistry meterRegistry;

    @Override
    public void initialize() {
        this.meterRegistry = metrics.prometheus()
            .pushgateway()
            .address()
            .map(this::createPrometheusMeterRegistry)
            .orElse(new SimpleMeterRegistry());
        registerCommonTags(meterRegistry);
        Gauge.builder(METER_JQASSISTANT_DISTRIBUTION, () -> 1)
            .tag(TAG_JQASSISTANT_VERSION, getVersionProvider().getVersion())
            .description("The jQAssistant distribution.")
            .strongReference(true)
            .register(meterRegistry);
        addRegistry(meterRegistry);
    }

    @Override
    public MeterRegistry getMeterRegistry() {
        return meterRegistry;
    }

    @Override
    public void destroy() throws IOException {
        if (pushGateway != null) {
            metrics.prometheus()
                .pushgateway()
                .address()
                .ifPresent(address -> log.info("Pushing collected metrics to Prometheus Pushgateway '{}'.", address));
            pushGateway.push();
        }
        meterRegistry.close();
    }

    private @NonNull MeterRegistry createPrometheusMeterRegistry(String address) {
        Prometheus.Pushgateway pushgateway = metrics.prometheus()
            .pushgateway();
        log.info("Initializing Prometheus metrics provider using address '{}' (scheme={}).", address, pushgateway.scheme());
        PrometheusMeterRegistry meterRegistry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        PushGateway.Builder builder = PushGateway.builder()
            .address(address)
            .scheme(Scheme.fromString(pushgateway.scheme()))
            .job(pushgateway.jobName())
            .registry(meterRegistry.getPrometheusRegistry());
        pushgateway.bearerToken()
            .ifPresent(builder::bearerToken);
        pushgateway.username()
            .ifPresent(username -> builder.basicAuth(username, pushgateway.password()
                .orElse(null)));
        this.pushGateway = builder.build();
        return meterRegistry;
    }

    private void registerCommonTags(MeterRegistry meterRegistry) {
        List<Tag> commonTags = metrics.commonTags()
            .entrySet()
            .stream()
            .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
            .collect(toList());
        meterRegistry.config()
            .commonTags(commonTags);
    }
}
