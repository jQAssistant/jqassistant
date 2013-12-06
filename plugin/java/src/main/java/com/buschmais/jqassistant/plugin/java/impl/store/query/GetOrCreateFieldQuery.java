package com.buschmais.jqassistant.plugin.java.impl.store.query;

import com.buschmais.cdo.neo4j.api.annotation.Cypher;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.FieldDescriptor;

@Cypher("match (t:TYPE) where id(t)=id({type}) create unique (t)-[:DECLARES]->(f:FIELD {SIGNATURE:{signature}}) return f as field")
public interface GetOrCreateFieldQuery {

    FieldDescriptor getField();
}
