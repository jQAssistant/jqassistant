package com.buschmais.jqassistant.core.plugin.api.configuration;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

@Description("The configuration for a plugin which can be resolved by Maven coordinates, i.e. groupId, artifactId, type, classifier and version. Furthermore a plugin can be declared as active or inactive.")
public interface Plugin {

    @Description("Activate the plugin (default: true).")
    @WithDefault("true")
    boolean active();

    @Description("The groupId of the plugin.")
    String groupId();

    @Description("The classifier of the plugin (optional).")
    Optional<String> classifier();

    @Description("The artifactId of the plugin.")
    List<String> artifactId();

    @Description("The type (extension) of the plugin (default: jar).")
    @WithDefault("jar")
    String type();

    @Description("The version of the plugin (default: jar).")
    String version();

}
