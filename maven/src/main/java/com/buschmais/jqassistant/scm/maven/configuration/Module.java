package com.buschmais.jqassistant.scm.maven.configuration;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

/**
 * Per-Module configuration, applies to module-based goals.
 */
@ConfigMapping(prefix = "jqassistant.maven.module")
public interface Module {

    String SKIP = "skip";

    @WithDefault("false")
    @Description("Skip execution of jQAssistant goals for a module.")
    boolean skip();

}
