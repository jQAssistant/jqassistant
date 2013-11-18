package com.buschmais.jqassistant.plugin.java.impl.store.query;

import com.buschmais.cdo.neo4j.api.annotation.Cypher;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.FieldDescriptor;

@Cypher("match (t:TYPE) where t={type} create unique (t)-[:CONTAINS]->(f:FIELD {FQN:{fqn}, SIGNATURE:{signature}}) return f as field")
public interface GetOrCreateFieldQuery {

	FieldDescriptor getField();
}
