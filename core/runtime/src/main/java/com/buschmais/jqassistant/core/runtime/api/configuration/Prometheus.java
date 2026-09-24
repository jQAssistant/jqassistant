package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.Optional;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * The Prometheus configuration.
 */
@ConfigMapping(prefix = Prometheus.PREFIX)
public interface Prometheus {

    String PREFIX = "jqassistant.metrics.prometheus";

    /**
     * Return the {@link Pushgateway} configuration.
     *
     * @return The {@link Pushgateway} configuration.
     */
    Pushgateway pushgateway();

    /**
     * The Pushgateway configuration.
     */
    @ConfigMapping(prefix = Prometheus.Pushgateway.PREFIX)
    interface Pushgateway {
        String PREFIX = "jqassistant.metrics.prometheus.pushgateway";

        String ADDRESS = "address";

        /**
         * Return the address of the Pushgateway, e.g. "localhost:9091"
         *
         * @return the address of the Pushgateway.
         */
        Optional<String> address();

        /**
         * Return the scheme to use for connecting to the Pushgateway: "http" or "https".
         *
         * @return The scheme to use for connectiong to the Pushgateway.
         */
        @WithDefault("http")
        String scheme();

        /**
         * Return the username for basic authentication.
         *
         * @return The username for basic authentication.
         */
        Optional<String> username();

        /**
         * Return the password for basic authentication.
         *
         * @return The password for basic authentication.
         */
        Optional<String> password();

        String BEARER_TOKEN = "bearer-token";

        /**
         * Return the bearer token for token based authentication.
         *
         * @return The bearer token for token based authentication.
         */
        Optional<String> bearerToken();

        /**
         * Return the job name to publish.
         *
         * @return The job name to publish.
         */
        @WithDefault("jQAssistant")
        String jobName();

    }

}
