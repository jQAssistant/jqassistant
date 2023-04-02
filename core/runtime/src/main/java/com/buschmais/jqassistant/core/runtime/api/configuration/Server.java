package com.buschmais.jqassistant.core.runtime.api.configuration;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.server")
public interface Server {

    String DAEMON = "daemon";

    @Description("Run the server as daemon.")
    @WithDefault("false")
    boolean daemon();

}
