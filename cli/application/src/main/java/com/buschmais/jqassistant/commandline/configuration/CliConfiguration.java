package com.buschmais.jqassistant.commandline.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

import static com.buschmais.jqassistant.core.runtime.api.configuration.Configuration.PREFIX;

@ConfigMapping(prefix = PREFIX)
@Description("The configuration for the Command Line Utility.")
public interface CliConfiguration extends Configuration {

    String PROXY = "proxy";

    @Description("The proxy to use for connecting to repositories.")
    Optional<Proxy> proxy();

    @Description("The repositories for resolving plugins and their dependencies.")
    Repositories repositories();

}
