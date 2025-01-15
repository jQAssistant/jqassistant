package com.buschmais.jqassistant.core.shared.aether.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping
@Description("Defines a dependency exclusion.")
public interface Exclusion {

    @Description("The groupId of the exclusion.")
    String groupId();

    @Description("The artifactId of the exclusion.")
    String artifactId();

    @Description("The classifier of the exclusion (optional).")
    Optional<String> classifier();

    @Description("The type (extension) of the exclusion.")
    @WithDefault("jar")
    String type();

}
