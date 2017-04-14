package com.buschmais.jqassistant.neo4jserver.bootstrap.impl;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.bootstrap.api.Server;
import com.buschmais.jqassistant.neo4jserver.bootstrap.spi.ServerFactory;

public class EmbeddedNeoServer implements Server {

    private Server delegate;

    public EmbeddedNeoServer(EmbeddedGraphStore store, String address, int port) {
        ServiceLoader<ServerFactory> serverFactories = ServiceLoader.load(ServerFactory.class);
        Iterator<ServerFactory> iterator = serverFactories.iterator();
        if (iterator.hasNext()) {
            ServerFactory serverFactory = iterator.next();
            delegate = serverFactory.getServer(store, address, port);
        } else {
            throw new IllegalStateException("Cannot find server factory.");
        }
    }

    @Override
    public void start() {
        delegate.start();
    }

    @Override
    public void stop() {
        delegate.stop();
    }
}
