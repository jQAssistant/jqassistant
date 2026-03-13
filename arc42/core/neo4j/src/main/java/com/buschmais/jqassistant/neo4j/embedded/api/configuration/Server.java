package com.buschmais.jqassistant.neo4j.embedded.api.configuration;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.server")
public interface Server {

    String DAEMON = "daemon";

    @Description("Run the server as daemon.")
    @WithDefault("false")
    boolean daemon();

    String OPEN_BROWSER = "open-browser";

    @Description("Open server URL in browser.")
    @WithDefault("false")
    boolean openBrowser();

}
