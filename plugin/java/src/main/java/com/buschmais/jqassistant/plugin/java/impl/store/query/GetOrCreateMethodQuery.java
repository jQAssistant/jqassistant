package com.buschmais.jqassistant.plugin.java.impl.store.query;

import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;
import com.buschmais.xo.neo4j.api.annotation.Cypher;

@Cypher("match (t:TYPE) where id(t)={type} create unique (t)-[:DECLARES]->(m:METHOD {SIGNATURE:{signature}}) return m as method")
public interface GetOrCreateMethodQuery {

    MethodDescriptor getMethod();
}
