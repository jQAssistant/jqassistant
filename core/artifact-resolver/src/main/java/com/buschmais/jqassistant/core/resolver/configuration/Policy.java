package com.buschmais.jqassistant.core.resolver.configuration;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

@Description("The repository policy for a Maven repository.")
public interface Policy {

    String ENABLED = "enabled";

    @WithDefault("true")
    boolean enabled();

    String UPDATE_POLICY = "update-policy";

    @WithDefault("daily")
    String updatePolicy();

    String CHECKSUM_POLICY = "checksum-policy";

    @WithDefault("warn")
    String checksumPolicy();

}
