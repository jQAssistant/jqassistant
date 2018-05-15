package com.buschmais.jqassistant.neo4j.backend.neo4jv2;

import java.util.Properties;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.AbstractEmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;

public class Neo4jV2ServerFactory extends AbstractEmbeddedNeo4jServerFactory {

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jV2CommunityNeoServer();
    }

    @Override
    protected void setXOUnitProperties(Properties properties) {
    }
}
