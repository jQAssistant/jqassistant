package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.net.InetSocketAddress;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.webapp.WebAppContext;

@Slf4j
class Neo4jCommunityNeoServer implements EmbeddedNeo4jServer {

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
        log.info("Starting HTTP server.");
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot start embedded server.", e);
        }
        log.info("Neo4j browser available at http://{}:{}?dbms=bolt://{}:{}&preselectAuthMethod=NO_AUTH.", listenAddress, httpPort, listenAddress, boltPort);
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
            log.info("Stopping HTTP server.");
            try {
                server.stop();
            } catch (Exception e) {
                throw new IllegalStateException("Cannot stop embedded server.", e);
            }
        }
    }
}
