package com.buschmais.jqassistant.core.runtime.api.configuration;

import java.util.Map;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = Metrics.PREFIX)
public interface Metrics {

    String PREFIX = "jqassistant.metrics";

    Prometheus prometheus();

    @WithDefault("jQAssistant")
    String jobName();

    String COMMON_TAGS = "common-tags";

    Map<String, String> commonTags();

}
