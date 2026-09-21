package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = Prometheus.PREFIX)
public interface Prometheus {

    String PREFIX = "jqassistant.metrics.prometheus";

    Pushgateway pushgateway();

    @ConfigMapping(prefix = Prometheus.Pushgateway.PREFIX)
    interface Pushgateway {
        String PREFIX = "jqassistant.metrics.prometheus.pushgateway";

        String ADDRESS = "address";
        Optional<String> address();

        @WithDefault("http")
        String scheme();

        Optional<String> username();

        Optional<String> password();

        String BEARER_TOKEN = "bearer-token";

        Optional<String> bearerToken();
    }

}
