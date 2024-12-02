package com.buschmais.jqassistant.core.test.plugin;

import com.buschmais.jqassistant.core.resolver.configuration.ArtifactResolverConfiguration;
import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = Configuration.PREFIX)
public interface ITConfiguration extends Configuration, ArtifactResolverConfiguration {
}
