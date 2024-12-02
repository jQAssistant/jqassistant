package com.buschmais.jqassistant.commandline.configuration;

import com.buschmais.jqassistant.core.resolver.configuration.ArtifactResolverConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

import static com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader.PREFIX;

@ConfigMapping(prefix = PREFIX)
@Description("The configuration for the Command Line Utility.")
public interface CliConfiguration extends Configuration, ArtifactResolverConfiguration {
}
