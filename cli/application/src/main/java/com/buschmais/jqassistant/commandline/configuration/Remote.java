package com.buschmais.jqassistant.commandline.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

@Description("The configuration for a Maven repository for providing plugins and required dependencies.")
public interface Remote {

    @Description("The URL of the Maven repository.")
    String url();

    @Description("The user name for authenticating against the repository.")
    Optional<String> username();

    @Description("The password for authenticating against the repository.")
    Optional<String> password();

}
