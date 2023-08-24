package com.buschmais.jqassistant.core.runtime.impl.configuration;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = Configuration.PREFIX)
public interface TestConfiguration extends Configuration {
}
