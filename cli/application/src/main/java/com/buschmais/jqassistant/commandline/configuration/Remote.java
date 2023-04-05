package com.buschmais.jqassistant.commandline.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

@Description("The configuration for a Maven repository for providing plugins and required dependencies.")
@ConfigMapping(prefix = Remote.PREFIX)
public interface Remote {

    String PREFIX = "jqassistant.repositories.remote";

    String URL = "url";

    @Description("The URL of the Maven repository.")
    String url();

    String USERNAME = "username";

    @Description("The user name for authenticating against the repository.")
    Optional<String> username();

    String PASSWORD = "password";

    @Description("The password for authenticating against the repository.")
    Optional<String> password();

}
