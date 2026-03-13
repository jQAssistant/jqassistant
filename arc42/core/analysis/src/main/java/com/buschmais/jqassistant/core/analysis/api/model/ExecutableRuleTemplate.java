package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.List;

import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.xo.neo4j.api.annotation.Relation;

public interface ExecutableRuleTemplate {

    Result.Status getStatus();

    void setStatus(Result.Status status);

    @Relation("REQUIRES_CONCEPT")
    List<ConceptDescriptor> getRequiresConcepts();

}
