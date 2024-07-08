package com.buschmais.jqassistant.core.analysis.api.model;

import com.buschmais.xo.neo4j.api.annotation.Label;

/**
 * Describes an executed concept.
 */
@Label("Constraint")
public interface ConstraintDescriptor extends RuleDescriptor, ExecutableRuleTemplate {
}
