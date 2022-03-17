package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.configuration.api.Configuration;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = Configuration.PREFIX)
public interface MavenConfiguration extends Configuration {

    String USE_EXECUTION_ROOT_AS_PROJECT_ROOT = "use-execution-root-as-project-root";

    @WithDefault("false")
    boolean useExecutionRootAsProjectRoot();

}
