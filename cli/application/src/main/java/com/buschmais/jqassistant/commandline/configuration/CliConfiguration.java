package com.buschmais.jqassistant.commandline.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "jqassistant")
@Description("The configuration for the Command Line Utility.")
public interface CliConfiguration extends Configuration {

    @Description("The repositories for resolving plugins and their dependencies.")
    Optional<Repositories> repositories();

}
