package com.buschmais.jqassistant.commandline.configuration;

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

}
