package com.buschmais.jqassistant.core.analysis.api.model;

import java.util.List;

import com.buschmais.xo.neo4j.api.annotation.Label;
import com.buschmais.xo.neo4j.api.annotation.Relation;

/**
 * Describes an executed group.
 */
@Label("Group")
public interface GroupDescriptor extends RuleDescriptor {

    @Relation("INCLUDES_CONCEPT")
    List<ConceptDescriptor> getIncludesConcepts();

    @Relation("INCLUDES_CONSTRAINT")
    List<ConstraintDescriptor> getIncludesConstraints();

    @Relation("INCLUDES_GROUP")
    List<GroupDescriptor> getIncludesGroups();
}
