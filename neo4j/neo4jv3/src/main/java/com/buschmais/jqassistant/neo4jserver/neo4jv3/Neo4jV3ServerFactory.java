package com.buschmais.jqassistant.neo4jserver.neo4jv3;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.bootstrap.api.Server;
import com.buschmais.jqassistant.neo4jserver.bootstrap.spi.ServerFactory;

public class Neo4jV3ServerFactory implements ServerFactory {

    @Override
    public Server getServer(EmbeddedGraphStore store, String address, int port) {
        return new Neo4jV3CommunityNeoServer(store, address, port);
    }

}
