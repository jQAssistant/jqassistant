package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes an executed concept.
 */
@Label("Constraint")
public interface ConstraintDescriptor extends RuleDescriptor, ExecutableRuleTemplate {

    Result.Status getStatus();

    void setStatus(Result.Status status);

    @Relation("PROVIDES_CONCEPT")
    List<ConstraintDescriptor> getProvidesConcepts();
}
