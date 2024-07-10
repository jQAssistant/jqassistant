package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes an executed constraint.
 */
@Label("Concept")
public interface ConceptDescriptor extends RuleDescriptor, ExecutableRuleTemplate {

    @Relation("PROVIDES_CONCEPT")
    List<ConceptDescriptor> getProvidesConcepts();
}
