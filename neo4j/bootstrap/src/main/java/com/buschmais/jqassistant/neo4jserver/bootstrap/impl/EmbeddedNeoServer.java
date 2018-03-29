package com.buschmais.jqassistant.neo4jserver.bootstrap.impl;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.buschmais.jqassistant.neo4jserver.bootstrap.api.ServerFactory;
import com.buschmais.jqassistant.neo4jserver.bootstrap.spi.Server;

import org.neo4j.graphdb.GraphDatabaseService;


public class EmbeddedNeoServer implements Server {

    private Server delegate;

    public EmbeddedNeoServer() {
        ServiceLoader<ServerFactory> serverFactories = ServiceLoader.load(ServerFactory.class);
        Iterator<ServerFactory> iterator = serverFactories.iterator();
        if (iterator.hasNext()) {
            ServerFactory serverFactory = iterator.next();
            delegate = serverFactory.getServer();
        } else {
            throw new IllegalStateException("Cannot find server factory.");
        }
    }

    @Override
    public void init(GraphDatabaseService graphDatabaseService) {
        delegate.init(graphDatabaseService);
    }

    @Override
    public void start(String httpAddress, int httpPort) {
        delegate.start(httpAddress, httpPort);
    }

    @Override
    public void stop() {
        delegate.stop();
    }
}
