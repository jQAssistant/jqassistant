package com.buschmais.jqassistant.plugin.java.impl.store.query;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.ParameterDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

@Cypher("match (m:METHOD)-[:HAS]->(p:PARAMETER) where id(m)=id({method}) and p.INDEX={index} return p as parameter")
public interface FindParameterQuery {

	ParameterDescriptor getParameter();
}
