package com.buschmais.jqassistant.commandline.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

public interface Proxy {

    @WithDefault("https")
    String protocol();

    String host();

    Integer port();

    @Description("The user name for authenticating against the proxy.")
    Optional<String> username();

    @Description("The password for authenticating against the proxy.")
    Optional<String> password();

}
