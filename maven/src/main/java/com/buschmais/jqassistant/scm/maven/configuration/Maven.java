package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.maven")
public interface Maven {

    String USE_EXECUTION_ROOT_AS_PROJECT_ROOT = "use-execution-root-as-project-root";

    @Description("Force the module where 'mvn' is being executed to be used as root module. The database will be created in this module and contain all information of the reactor. Rules will be read from the rules of this module.")
    @WithDefault("true")
    boolean useExecutionRootAsProjectRoot();

}
