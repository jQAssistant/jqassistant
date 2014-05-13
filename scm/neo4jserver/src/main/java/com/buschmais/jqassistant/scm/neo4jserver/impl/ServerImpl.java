package com.buschmais.jqassistant.scm.neo4jserver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.neo4j.server.WrappingNeoServer;
import org.neo4j.server.database.InjectableProvider;
import org.neo4j.server.modules.ServerModule;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;

/**
 * The customized Neo4j server.
 * <p>
 * The class adds the {@link JQAServerModule}
 * </p>
 */
public class ServerImpl extends WrappingNeoServer implements Server {

    private final Store store;

    /**
     * Constructor.
     * 
     * @param graphStore
     *            The store instance to use.
     */
    public ServerImpl(EmbeddedGraphStore graphStore) {
        super(graphStore.getDatabaseService());
        this.store = graphStore;
    }

    @Override
    protected Iterable<ServerModule> createServerModules() {
        List<ServerModule> serverModules = new ArrayList<>();
        serverModules.add(new JQAServerModule(webServer));
        for (ServerModule serverModule : super.createServerModules()) {
            serverModules.add(serverModule);
        }
        return serverModules;
    }

    @Override
    protected Collection<InjectableProvider<?>> createDefaultInjectables() {
        Collection<InjectableProvider<?>> defaultInjectables = super.createDefaultInjectables();
        defaultInjectables.add(new StoreProvider(store));
        return defaultInjectables;
    }
}
