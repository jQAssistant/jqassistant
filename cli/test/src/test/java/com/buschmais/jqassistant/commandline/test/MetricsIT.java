package com.buschmais.jqassistant.commandline.test;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import com.buschmais.jqassistant.core.runtime.api.metrics.MeterRegistryFactory;

import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies metrics using Prometheus Pushgateway
 */
@Testcontainers
class MetricsIT extends AbstractCLIIT {

    public static final int PUSHGATEWAY_PORT = 9091;

    @Container
    public static GenericContainer<?> pushgatewayContainer = new GenericContainer<>("prom/pushgateway:v1.9.0").withExposedPorts(PUSHGATEWAY_PORT)
        .waitingFor(Wait.forHttp("/-/ready")
            .forPort(PUSHGATEWAY_PORT));

    @DistributionTest
    void metrics() {
        String pushgatewayAddress = "localhost:" + pushgatewayContainer.getMappedPort(PUSHGATEWAY_PORT);
        String[] args = new String[] { "analyze", "-D", "jqassistant.analyze.rule.directory=" + RULES_DIRECTORY, "-D",
            "jqassistant.metrics.prometheus.pushgateway.address=" + pushgatewayAddress };
        assertThat(execute(args).getExitCode()).isEqualTo(2);

        WebTarget webTarget = ClientBuilder.newClient()
            .target("http://" + pushgatewayAddress)
            .path("/metrics");
        String response = webTarget.request()
            .get(String.class);
        assertThat(response).contains(MeterRegistryFactory.METER_JQASSISTANT_DISTRIBUTION);
    }

}
