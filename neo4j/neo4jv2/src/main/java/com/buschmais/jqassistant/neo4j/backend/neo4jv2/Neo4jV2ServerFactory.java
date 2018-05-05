package com.buschmais.jqassistant.neo4j.backend.neo4jv2;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServerFactory;

public class Neo4jV2ServerFactory implements EmbeddedNeo4jServerFactory {

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jV2CommunityNeoServer();
    }

}
