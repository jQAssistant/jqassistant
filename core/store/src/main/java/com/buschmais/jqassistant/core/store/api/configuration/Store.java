package com.buschmais.jqassistant.core.store.api.configuration;

import java.net.URI;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jqassistant.store")
public interface Store {

    String URI = "uri";

    @Description("URI of the database to connect to. Supported URI schemes are 'file' for embedded databases and 'bolt' for connecting to a running Neo4j instance (3.x+).")
    Optional<URI> uri();

    @Description("Configuration of the embedded store.")
    Embedded embedded();

    @Description("Configuration of the remote store.")
    Remote remote();

}
