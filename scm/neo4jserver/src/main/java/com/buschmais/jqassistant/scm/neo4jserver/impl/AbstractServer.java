package com.buschmais.jqassistant.scm.neo4jserver.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.buschmais.jqassistant.core.plugin.api.RulePluginRepository;
import com.buschmais.jqassistant.core.plugin.api.ScannerPluginRepository;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.scm.neo4jserver.api.Server;
import com.buschmais.jqassistant.scm.neo4jserver.impl.rest.AnalysisService;
import com.buschmais.jqassistant.scm.neo4jserver.impl.rest.MetricsService;
import com.buschmais.jqassistant.scm.neo4jserver.impl.rest.VersionService;

import com.sun.jersey.api.core.HttpContext;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.server.NeoServer;
import org.neo4j.server.WrappingNeoServer;
import org.neo4j.server.database.InjectableProvider;
import org.neo4j.server.modules.ServerModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class for the customized Neo4j server.
 *
 * The class adds the {@link JQAServerModule}
 */
public abstract class AbstractServer extends WrappingNeoServer implements Server {

    /**
     * The logger class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServer.class);

    protected final Store store;

    public AbstractServer(EmbeddedGraphStore graphStore) {
        super((GraphDatabaseAPI) graphStore.getGraphDatabaseService());
        this.store = graphStore;
    }

    @Override
    protected Iterable<ServerModule> createServerModules() {

        List<String> extensionNames = new ArrayList<>();
        extensionNames.add(VersionService.class.getName());
        extensionNames.add(AnalysisService.class.getName());
        extensionNames.add(MetricsService.class.getName());

        for (Class<?> extension : getExtensions()) {
            extensionNames.add(extension.getName());
        }

        if (LOGGER.isInfoEnabled()) {
            for (String extensionName : extensionNames) {
                LOGGER.info("Registering extension: " + extensionName);
            }
        }

        List<ServerModule> serverModules = new ArrayList<>();
        serverModules.add(new JQAServerModule(webServer, extensionNames));

        for (ServerModule serverModule : super.createServerModules()) {
            serverModules.add(serverModule);
        }

        return serverModules;
    }

    @Override
    protected Collection<InjectableProvider<?>> createDefaultInjectables() {
        Collection<InjectableProvider<?>> defaultInjectables = super.createDefaultInjectables();
        addInjectable(Store.class, store, defaultInjectables);
        addInjectable(RulePluginRepository.class, getRulePluginRepository(), defaultInjectables);
        addInjectable(ScannerPluginRepository.class, getScannerPluginRepository(), defaultInjectables);
        addInjectable(NeoServer.class, this, defaultInjectables);
        return defaultInjectables;
    }

    private <T> void addInjectable(Class<T> type, final T injectable, Collection<InjectableProvider<?>> defaultInjectables) {
        defaultInjectables.add(new InjectableProvider<T>(type) {
            @Override
            public T getValue(HttpContext c) {
                return injectable;
            }
        });
    }

    /**
     * Return the extension classes that shall be included.
     * 
     * @return The extension classes.
     */
    protected abstract Iterable<? extends Class<?>> getExtensions();

    protected abstract ScannerPluginRepository getScannerPluginRepository();

    protected abstract RulePluginRepository getRulePluginRepository();
}
