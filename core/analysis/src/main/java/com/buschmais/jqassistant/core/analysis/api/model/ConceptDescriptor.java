package com.buschmais.jqassistant.core.analysis.api.model;

import java.lang.annotation.Retention;
import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;
import com.buschmais.xo.neo4j.api.annotation.Relation.Incoming;
import com.buschmais.xo.neo4j.api.annotation.Relation.Outgoing;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Describes an executed constraint.
 */
@Label("Concept")
public interface ConceptDescriptor extends RuleDescriptor, ExecutableRuleTemplate {

    @Outgoing
    @ProvidesConcept
    List<ConceptDescriptor> getProvidesConcepts();

    @Incoming
    @ProvidesConcept
    List<ConceptDescriptor> getProvidingConcepts();

    @Relation("PROVIDES_CONCEPT")
    @Retention(RUNTIME)
    @interface ProvidesConcept {
    }
}
