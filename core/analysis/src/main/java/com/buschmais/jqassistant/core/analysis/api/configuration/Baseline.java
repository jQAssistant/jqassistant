package com.buschmais.jqassistant.core.analysis.api.configuration;

import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.analyze.baseline")
public interface Baseline {

    @WithDefault("false")
    boolean enabled();

    String FILE = "file";

    @Description("The name of the file for reading and writing the baseline.")
    Optional<String> file();

    String INCLUDE_CONCEPTS = "include-concepts";

    @Description("The concepts to be included.")
    Optional<List<String>> includeConcepts();

    String INCLUDE_CONSTRAINTS = "include-constraints";

    @Description("The constraints to be included.")
    @WithDefault("*")
    List<String> includeConstraints();

}
