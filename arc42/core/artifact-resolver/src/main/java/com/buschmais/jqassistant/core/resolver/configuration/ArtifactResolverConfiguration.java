package com.buschmais.jqassistant.core.resolver.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;
import com.buschmais.jqassistant.core.shared.configuration.ConfigurationMappingLoader;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = ConfigurationMappingLoader.PREFIX)
public interface ArtifactResolverConfiguration {

    String PREFIX = "jqassistant";

    String PROXY = "proxy";

    @Description("The proxy to use for connecting to repositories.")
    Optional<Proxy> proxy();

    @Description("The repositories for resolving plugins and their dependencies.")
    Repositories repositories();

}
