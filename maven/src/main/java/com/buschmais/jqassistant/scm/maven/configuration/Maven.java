package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * The Maven configuration.
 */
@ConfigMapping(prefix = "jqassistant.maven")
public interface Maven {

    /**
     * The per-{@link Module} configuration.
     *
     * @return The {@link Module} configuration.
     */
    Module module();

    String USE_EXECUTION_ROOT_AS_PROJECT_ROOT = "use-execution-root-as-project-root";

    @Description("Force the module where 'mvn' is being executed to be used as root module. The database will be created in this module and contain all information of the reactor. Rules will be read from the rules of this module.")
    @WithDefault("true")
    boolean useExecutionRootAsProjectRoot();

    String REUSE_STORE = "reuse-store";

    @WithDefault("true")
    @Description("Re-use store instances across all modules of the Maven reactor. Can be set to false for mitigating problems in specific setups, the jQAssistant Maven plugin will display an according hint when this is required.")
    boolean reuseStore();
}
