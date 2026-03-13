package com.buschmais.jqassistant.core.analysis.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes an executed concept.
 */
@Label("Constraint")
public interface ConstraintDescriptor extends RuleDescriptor, ExecutableRuleTemplate {

    @Relation("OVERRIDES_CONSTRAINT")
    ConstraintDescriptor getOverridesConstraint();

    void setOverridesConstraint(ConstraintDescriptor constraintDescriptor);

}
