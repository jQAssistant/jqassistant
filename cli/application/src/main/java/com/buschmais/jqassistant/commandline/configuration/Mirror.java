package com.buschmais.jqassistant.commandline.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

@Description("The configuration for a Maven repository for providing plugins and required dependencies.")
@ConfigMapping(prefix = Mirror.PREFIX)
public interface Mirror {

    String PREFIX = "jqassistant.repositories.mirrors";

    String URL = "url";

    @Description("The mirror URL.")
    String url();

    String MIRROR_OF = "mirror-of";

    @Description("The identifier(s) of remote repositories to mirror.")
    String mirrorOf();

    String USERNAME = "username";

    @Description("The user name for authenticating against the mirror.")
    Optional<String> username();

    String PASSWORD = "password";

    @Description("The password for authenticating against the mirror.")
    Optional<String> password();

}
