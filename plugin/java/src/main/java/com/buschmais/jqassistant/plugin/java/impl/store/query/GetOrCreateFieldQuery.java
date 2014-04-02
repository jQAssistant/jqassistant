package com.buschmais.jqassistant.plugin.java.impl.store.query;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.FieldDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

@Cypher("match (t:TYPE) where id(t)={type} create unique (t)-[:DECLARES]->(f:FIELD {SIGNATURE:{signature}}) return f as field")
public interface GetOrCreateFieldQuery {

    FieldDescriptor getField();
}
