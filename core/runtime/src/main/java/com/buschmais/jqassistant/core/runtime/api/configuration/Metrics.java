package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.Map;

import io.smallrye.config.ConfigMapping;

/**
 * The metrics configuration.
 */
@ConfigMapping(prefix = Metrics.PREFIX)
public interface Metrics {

    String PREFIX = "jqassistant.metrics";

    /**
     * Return the {@link Prometheus} configuration.
     *
     * @return The {@link Prometheus} configuration.
     */
    Prometheus prometheus();

    String COMMON_TAGS = "common-tags";

    /**
     * Return the common tags to be used for all published metrics.
     *
     * @return The common tags.
     */
    Map<String, String> commonTags();

}
