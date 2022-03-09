package com.buschmais.jqassistant.core.rule.api.configuration;

import io.smallrye.config.WithDefault;

public interface Rule {

    String PREFIX = "jqassistant.analyze.rule";

    String REQUIRED_CONCEPTS_ARE_OPTIONAL_BY_DEFAULT = "required-concepts-are-optional-by-default";

    @WithDefault("true")
    boolean requiredConceptsAreOptionalByDefault();

}
