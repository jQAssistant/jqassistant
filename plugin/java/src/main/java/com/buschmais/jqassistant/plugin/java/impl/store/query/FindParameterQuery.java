package com.buschmais.jqassistant.plugin.java.impl.store.query;

import com.buschmais.cdo.neo4j.api.annotation.Cypher;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ParameterDescriptor;

@Cypher("match (m:METHOD)-[:HAS]->(p:PARAMETER) where m={method} and p.INDEX={index} return p as parameter")
public interface FindParameterQuery {

	ParameterDescriptor getParameter();
}
