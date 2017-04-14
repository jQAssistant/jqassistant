package com.buschmais.jqassistant.neo4jserver.bootstrap.spi;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.bootstrap.api.Server;

public interface ServerFactory {

    Server getServer(EmbeddedGraphStore store, String address, int port);

}
