package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface ExecutableRuleTemplate {

    @Relation("REQUIRES_CONCEPT")
    List<ConceptDescriptor> getRequiresConcepts();

}
