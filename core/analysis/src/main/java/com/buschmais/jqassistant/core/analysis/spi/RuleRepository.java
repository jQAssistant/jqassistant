package com.buschmais.jqassistant.core.analysis.spi;

import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.xo.api.annotation.Repository;
import com.buschmais.xo.api.annotation.ResultOf;
import com.buschmais.xo.api.annotation.ResultOf.Parameter;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

@Repository
public interface RuleRepository {

    @ResultOf
    @Cypher("MATCH (concept:jQAssistant:Rule:Concept{id: $id}) RETURN concept")
    ConceptDescriptor find(@Parameter("id") String id);

    @ResultOf
    @Cypher("MERGE (concept:jQAssistant:Rule:Concept{id: $id}) RETURN concept")
    ConceptDescriptor merge(@Parameter("id") String id);

}
