package com.buschmais.jqassistant.core.store.api.configuration;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.configuration.Embedded;

import io.smallrye.config.WithDefault;

public interface Store {

    String PREFIX = "jqassistant.store";

    String URI = "uri";

    Optional<URI> uri();

    String USERNAME = "username";

    Optional<String> username();

    String PASSWORD = "password";

    Optional<String> password();

    String ENCRYPTION = "encryption";

    @WithDefault("false")
    boolean encryption();

    String TRUST_STRATEGY = "trust-strategy";

    Optional<String> trustStrategy();

    String TRUST_CERTIFICATE = "trust-certificate";

    Optional<String> trustCertificate();

    String PROPERTIES = "properties";

    Map<String, String> properties();

    Embedded embedded();

}
