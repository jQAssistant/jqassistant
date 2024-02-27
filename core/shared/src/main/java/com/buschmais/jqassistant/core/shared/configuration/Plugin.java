package com.buschmais.jqassistant.core.shared.configuration;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping
@Description("The configuration for a plugin which can be resolved by Maven coordinates, i.e. groupId, artifactId, type, classifier and version.")
public interface Plugin {

    @Description("The groupId of the plugin.")
    String groupId();

    @Description("The classifier of the plugin (optional).")
    Optional<String> classifier();

    @Description("The artifactId of the plugin.")
    List<String> artifactId();

    @Description("The type (extension) of the plugin.")
    @WithDefault("jar")
    String type();

    @Description("The version of the plugin.")
    String version();

    @Description("The exclusions of the plugin.")
    List<Exclusion> exclusions();
}
