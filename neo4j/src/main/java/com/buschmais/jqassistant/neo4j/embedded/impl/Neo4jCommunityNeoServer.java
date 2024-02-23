package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.net.InetSocketAddress;
import java.util.Collection;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.neo4j.common.DependencyResolver;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.exceptions.KernelException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.api.procedure.GlobalProcedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
class Neo4jCommunityNeoServer implements EmbeddedNeo4jServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jCommunityNeoServer.class);
    private EmbeddedDatastore embeddedDatastore;

    private ClassLoader classLoader;

    private Server server;
    private String listenAddress;
    private Integer httpPort;

    @Override
    public String getVersion() {
        return "4.x";
    }

    @Override
    public final void initialize(EmbeddedDatastore embeddedDatastore, String listenAddress, Integer httpPort, ClassLoader classLoader,
        Collection<Class<?>> procedureTypes,
        Collection<Class<?>> functionTypes) {
        this.embeddedDatastore = embeddedDatastore;
        this.classLoader = classLoader;
        this.listenAddress = listenAddress;
        this.httpPort = httpPort;
        registerProceduresAndFunctions(procedureTypes, functionTypes);
    }

    @Override
    public void start() {
        this.server = new Server(new InetSocketAddress(listenAddress, httpPort));
        WebAppContext rootContext = getWebAppContext("/", "browser/");
        WebAppContext pluginContext = getWebAppContext("/jqassistant", "META-INF/jqassistant-static-content/");
        server.setHandler(new HandlerCollection(rootContext, pluginContext));
        LOGGER.info("Starting HTTP server.");
        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Cannot start embedded server.", e);
        }
        LOGGER.info("Neo4j browser available at http://{}:{}.", listenAddress, httpPort);
    }

    private WebAppContext getWebAppContext(String contextPath, String resourceRoot) {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath(contextPath);
        Resource resource = Resource.newResource(classLoader.getResource(resourceRoot));
        webAppContext.setBaseResource(resource);
        return webAppContext;
    }

    @Override
    public void stop() {
        if (server != null) {
            LOGGER.info("Stopping HTTP server.");
            try {
                server.stop();
            } catch (Exception e) {
                throw new RuntimeException("Cannot stop embedded server.", e);
            }
        }
    }

    /**
     * @deprecated Replaced by Neo4j plugins mechanism.
     */
    @Deprecated
    private void registerProceduresAndFunctions(Collection<Class<?>> procedureTypes, Collection<Class<?>> functionTypes) {
        GraphDatabaseService graphDatabaseService = embeddedDatastore.getManagementService()
            .database(GraphDatabaseSettings.DEFAULT_DATABASE_NAME);
        GlobalProcedures procedures = ((GraphDatabaseAPI) graphDatabaseService).getDependencyResolver()
            .resolveDependency(GlobalProcedures.class, DependencyResolver.SelectionStrategy.SINGLE);
        if (!procedureTypes.isEmpty() || !functionTypes.isEmpty()) {
            log.warn(
                "Explicit registration of Neo4j procedures and functions has been deprecated, please use the plugin mechanism provided by the embedded store.");
        }
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
