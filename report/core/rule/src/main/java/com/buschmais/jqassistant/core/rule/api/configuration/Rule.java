package com.buschmais.jqassistant.core.rule.api.configuration;

import java.util.Optional;

import com.buschmais.jqassistant.core.rule.api.model.Severity;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.analyze.rule")
public interface Rule {

    String DIRECTORY = "directory";

    @Description("The name of the directory containing project rules.")
    Optional<String> directory();

    String DEFAULT_CONCEPT_SEVERITY = "default-concept-severity";

    @Description("The default severity of concepts without an explicit severity.")
    Optional<Severity> defaultConceptSeverity();

    String DEFAULT_CONSTRAINT_SEVERITY = "default-constraint-severity";

    @Description("The default severity of constraints without an explicit severity.")
    Optional<Severity> defaultConstraintSeverity();

    String DEFAULT_GROUP_SEVERITY = "default-group-severity";

    @Description("The default severity of groups without an explicit severity.")
    Optional<Severity> defaultGroupSeverity();

    String REQUIRED_CONCEPTS_ARE_OPTIONAL_BY_DEFAULT = "required-concepts-are-optional-by-default";

    @Description("Specifies if a rule is executed even if one of the required concepts cannot be applied.")
    @WithDefault("true")
    boolean requiredConceptsAreOptionalByDefault();

}
