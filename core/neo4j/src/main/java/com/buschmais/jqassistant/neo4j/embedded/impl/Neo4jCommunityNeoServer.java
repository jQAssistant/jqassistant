package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.net.InetSocketAddress;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.xo.neo4j.embedded.impl.datastore.EmbeddedDatastore;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
class Neo4jCommunityNeoServer implements EmbeddedNeo4jServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jCommunityNeoServer.class);

    private ClassLoader classLoader;

    private Server server;
    private String listenAddress;
    private Integer httpPort;
    private Integer boltPort;

    @Override
    public final void initialize(String listenAddress, Integer httpPort, Integer boltPort, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.listenAddress = listenAddress;
        this.httpPort = httpPort;
        this.boltPort = boltPort;
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
            throw new IllegalStateException("Cannot start embedded server.", e);
        }
        LOGGER.info("Neo4j browser available at http://{}:{}?dbms=bolt://{}:{}&preselectAuthMethod=NO_AUTH.", listenAddress, httpPort, listenAddress, boltPort);
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
                throw new IllegalStateException("Cannot stop embedded server.", e);
            }
        }
    }
}
