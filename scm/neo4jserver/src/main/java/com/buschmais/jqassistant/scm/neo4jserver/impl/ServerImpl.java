package com.buschmais.jqassistant.scm.neo4jserver.impl;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import org.neo4j.server.WrappingNeoServer;
import org.neo4j.server.modules.ServerModule;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;

/**
 * The customized Neo4j server.
 * <p>
 * The class adds the {@link JQAssistantServerModule}
 * </p>
 */
public class ServerImpl extends WrappingNeoServer implements Server {

    public ServerImpl(EmbeddedGraphStore graphStore) {
        super(graphStore.getDatabaseService());
    }

    @Override
    protected Iterable<ServerModule> createServerModules() {
        List<ServerModule> serverModules = new ArrayList<>();
        serverModules.add(new JQAssistantServerModule(webServer));
        for (ServerModule serverModule : super.createServerModules()) {
            serverModules.add(serverModule);
        }
        return serverModules;
    }

}
