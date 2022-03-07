package com.buschmais.jqassistant.commandline.configuration;

import com.buschmais.jqassistant.core.configuration.api.Configuration;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

import static com.buschmais.jqassistant.core.configuration.api.Configuration.PREFIX;

@ConfigMapping(prefix = PREFIX)
@Description("The configuration for the Command Line Utility.")
public interface CliConfiguration extends Configuration {

    @Description("The repositories for resolving plugins and their dependencies.")
    Repositories repositories();

}
