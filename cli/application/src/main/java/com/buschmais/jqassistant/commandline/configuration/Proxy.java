package com.buschmais.jqassistant.commandline.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

public interface Proxy {

    String PROTOCOL = "protocol";

    @WithDefault("https")
    String protocol();

    String HOST = "host";
    String host();

    String PORT = "port";

    Integer port();

    String USERNAME = "username";
    @Description("The user name for authenticating against the proxy.")
    Optional<String> username();

    String PASSWORD = "password";
    @Description("The password for authenticating against the proxy.")
    Optional<String> password();

}
