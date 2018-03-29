package com.buschmais.jqassistant.neo4jserver.neo4jv2;

import com.buschmais.jqassistant.neo4jserver.bootstrap.api.ServerFactory;
import com.buschmais.jqassistant.neo4jserver.bootstrap.spi.Server;

public class Neo4jV2ServerFactory implements ServerFactory {
    @Override
    public Server getServer() {
        return new Neo4jV2CommunityNeoServer();
    }
}
