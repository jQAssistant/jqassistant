package com.buschmais.jqassistant.core.rule.api.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.rule.api.model.Severity;

import io.smallrye.config.WithDefault;

public interface Rule {

    String PREFIX = "jqassistant.analyze.rule";

    String DEFAULT_CONCEPT_SEVERITY = "default-concept-severity";

    Optional<Severity> defaultConceptSeverity();

    String DEFAULT_CONSTRAINT_SEVERITY = "default-constraint-severity";

    Optional<Severity> defaultConstraintSeverity();

    String DEFAULT_GROUP_SEVERITY = "default-group-severity";

    Optional<Severity> defaultGroupSeverity();

    String REQUIRED_CONCEPTS_ARE_OPTIONAL_BY_DEFAULT = "required-concepts-are-optional-by-default";

    @WithDefault("true")
    boolean requiredConceptsAreOptionalByDefault();

}
