package com.buschmais.jqassistant.core.store.api.configuration;

import java.util.Map;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.store.remote")
public interface Remote {

    String USERNAME = "username";

    @Description("The username 'bolt' connections.")
    Optional<String> username();

    String PASSWORD = "password";

    @Description("The password 'bolt' connections.")
    Optional<String> password();

    String ENCRYPTION = "encryption";

    @Description("Activate encryption level for 'bolt' connections.")
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

}
