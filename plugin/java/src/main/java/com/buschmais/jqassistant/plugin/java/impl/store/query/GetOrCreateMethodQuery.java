package com.buschmais.jqassistant.plugin.java.impl.store.query;

import com.buschmais.cdo.neo4j.api.annotation.Cypher;
import com.buschmais.jqassistant.plugin.java.impl.store.descriptor.MethodDescriptor;

@Cypher("match (t:TYPE) where id(t)=id({type}) create unique (t)-[:DECLARES]->(m:METHOD {SIGNATURE:{signature}}) return m as method")
public interface GetOrCreateMethodQuery {

    MethodDescriptor getMethod();
}
