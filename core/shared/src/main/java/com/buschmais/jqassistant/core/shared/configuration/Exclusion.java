package com.buschmais.jqassistant.core.shared.configuration;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

public interface Exclusion {

    @Description("The groupId of the exclusion.")
    String groupId();

    @Description("The classifier of the plugin (optional).")
    Optional<String> classifier();

    @Description("The artifactId of the plugin.")
    List<String> artifactId();

    @Description("The type (extension) of the plugin.")
    @WithDefault("jar")
    String type();

}
