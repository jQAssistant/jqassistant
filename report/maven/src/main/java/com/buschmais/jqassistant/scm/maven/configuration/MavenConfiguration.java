package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.runtime.api.configuration.Configuration;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = Configuration.PREFIX)
public interface MavenConfiguration extends Configuration {

    @Description("The Maven configuration.")
    Maven maven();

}
