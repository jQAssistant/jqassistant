package com.buschmais.jqassistant.core.store.api.configuration;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.configuration.Embedded;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.store")
public interface Store {

    String URI = "uri";

    @Description("URI of the database to connect to. Supported URI schemes are 'file' for embedded databases and 'bolt' for connecting to a running Neo4j instance (3.x+).")
    Optional<URI> uri();

    String USERNAME = "username";

    @Description("The username 'bolt' connections.")
    Optional<String> username();

    String PASSWORD = "password";

    @Description("The password 'bolt' connections.")
    Optional<String> password();

    String ENCRYPTION = "encryption";

    @Description("Activate encryption level 'bolt' connections.")
    @WithDefault("false")
    boolean encryption();

    String TRUST_STRATEGY = "trust-strategy";

    @Description("The trust strategy for 'bolt' connections: trustAllCertificates (default), trustCustomCaSignedCertificates or trustSystemCaSignedCertificates.")
    Optional<String> trustStrategy();

    String TRUST_CERTIFICATE = "trust-certificate";

    @Description("The file containing the custom CA certificate for trust strategy.")
    Optional<String> trustCertificate();

    String PROPERTIES = "properties";

    @Description("Additional properties to be passed to the store.")
    Map<String, String> properties();

    @Description("Configuration of the embedded store.")
    Embedded embedded();

}
