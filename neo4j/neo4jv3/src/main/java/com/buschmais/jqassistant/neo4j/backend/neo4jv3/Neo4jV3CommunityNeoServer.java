package com.buschmais.jqassistant.neo4j.backend.neo4jv3;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.AbstractEmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.backend.neo4jv3.extension.StaticContentResource;

import org.neo4j.graphdb.facade.GraphDatabaseDependencies;
import org.neo4j.internal.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Level;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.database.GraphFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.neo4j.graphdb.DependencyResolver.SelectionStrategy.ONLY;

public class Neo4jV3CommunityNeoServer extends AbstractEmbeddedNeo4jServer {

    public static final String HTTP_TYPE = "HTTP";

    private static final String DBMS_CONNECTOR_BOLT_ENABLED = "dbms.connector.bolt.enabled";
    private static final String DBMS_CONNECTOR_BOLT_LISTEN_ADDRESS = "dbms.connector.bolt.listen_address";
    private static final String DBMS_CONNECTOR_HTTP_ENABLED = "dbms.connector.http.enabled";
    private static final String DBMS_CONNECTOR_HTTP_LISTEN_ADDRESS = "dbms.connector.http.listen_address";
    private static final String DBMS_CONNECTOR_HTTP_TYPE = "dbms.connector.http.type";
    private static final String DBMS_UNMANAGED_EXTENSION_CLASSES = "dbms.unmanaged_extension_classes";

    private static final String STATIC_CONTENT_ROOT = "/jqassistant/";

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jV3CommunityNeoServer.class);

    private CommunityNeoServer communityNeoServer;

    @Override
    public String getVersion() {
        return "3.x";
    }

    @Override
    public void start() {
        Map<String, String> opts = new HashMap<>();
        // Neo4j 3.x
        opts.put(DBMS_CONNECTOR_HTTP_TYPE, HTTP_TYPE);
        opts.put(DBMS_CONNECTOR_HTTP_ENABLED, Boolean.TRUE.toString());
        opts.put(DBMS_CONNECTOR_HTTP_LISTEN_ADDRESS, embeddedNeo4jConfiguration.getListenAddress() + ":" + embeddedNeo4jConfiguration.getHttpPort());
        opts.put(DBMS_CONNECTOR_BOLT_ENABLED, Boolean.TRUE.toString());
        opts.put(DBMS_CONNECTOR_BOLT_LISTEN_ADDRESS, embeddedNeo4jConfiguration.getListenAddress() + ":" + embeddedNeo4jConfiguration.getBoltPort());
        opts.put(DBMS_UNMANAGED_EXTENSION_CLASSES, StaticContentResource.class.getPackage().getName() + "=" + STATIC_CONTENT_ROOT);

        Config defaults = Config.defaults(opts);
        FormattedLogProvider logProvider = FormattedLogProvider.withDefaultLogLevel(Level.INFO).toOutputStream(System.out);
        final GraphDatabaseDependencies graphDatabaseDependencies = GraphDatabaseDependencies.newDependencies().userLogProvider(logProvider);
        GraphFactory graphFactory = (config, dependencies) -> (GraphDatabaseFacade) embeddedDatastore.getGraphDatabaseService();
        communityNeoServer = new CommunityNeoServer(defaults, graphFactory, graphDatabaseDependencies);
        communityNeoServer.start();
    }

    @Override
    public void stop() {
        communityNeoServer.stop();
    }

    @Override
    protected void initialize(Collection<Class<?>> procedureTypes, Collection<Class<?>> functionTypes) {
        Procedures procedures = ((GraphDatabaseAPI) embeddedDatastore.getGraphDatabaseService()).getDependencyResolver()
            .resolveDependency(Procedures.class, ONLY);
        for (Class<?> procedureType : procedureTypes) {
            try {
                LOGGER.debug("Registering procedure class " + procedureType.getName());
                procedures.registerProcedure(procedureType);
            } catch (KernelException e) {
                LOGGER.warn("Cannot register procedure class " + procedureType.getName(), e);
            }
        }
        for (Class<?> functionType : functionTypes) {
            try {
                LOGGER.debug("Registering function class " + functionType.getName());
                procedures.registerFunction(functionType);
            } catch (KernelException e) {
                LOGGER.warn("Cannot register function class " + functionType.getName(), e);
            }
        }
    }
}
