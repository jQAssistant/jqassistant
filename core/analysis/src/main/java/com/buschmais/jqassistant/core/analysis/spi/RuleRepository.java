package com.buschmais.jqassistant.core.analysis.spi;

import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.analysis.api.model.ConstraintDescriptor;
import com.buschmais.jqassistant.core.analysis.api.model.GroupDescriptor;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

@Repository
public interface RuleRepository {

    @ResultOf
    @Cypher("MATCH (concept:jQAssistant:Rule:Concept{id: $id}) RETURN concept")
    ConceptDescriptor findConcept(@Parameter("id") String id);

    @ResultOf
    @Cypher("MERGE (concept:jQAssistant:Rule:Concept{id: $id}) RETURN concept")
    ConceptDescriptor mergeConcept(@Parameter("id") String id);

    @ResultOf
    @Cypher("MERGE (constraint:jQAssistant:Rule:Constraint{id: $id}) RETURN constraint")
    ConstraintDescriptor mergeConstraint(@Parameter("id") String id);

    @ResultOf
    @Cypher("MERGE (group:jQAssistant:Rule:Group{id: $id}) RETURN group")
    GroupDescriptor mergeGroup(@Parameter("id") String id);

}
