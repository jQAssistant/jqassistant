package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;

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
    private String url;

    @Override
    public void initialize(String listenAddress, Integer httpPort, Integer boltPort, ClassLoader classLoader) {
        this.classLoader = classLoader;
        this.server = new Server(new InetSocketAddress(listenAddress, httpPort));
        WebAppContext rootContext = getWebAppContext("/", "browser/");
        WebAppContext pluginContext = getWebAppContext("/jqassistant", "META-INF/jqassistant-static-content/");
        this.server.setHandler(new HandlerCollection(rootContext, pluginContext));
        this.url = String.format("http://%s:%d?dbms=bolt://%s:%d&preselectAuthMethod=NO_AUTH", listenAddress, httpPort, listenAddress, boltPort);
    }

    @Override
    public void start() {
        LOGGER.info("Starting HTTP server.");
        try {
            server.start();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot start embedded server.", e);
        }
        LOGGER.info("Neo4j browser available at {}.", url);
    }

    @Override
    public void openBrowser() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
            .isSupported(Desktop.Action.BROWSE)) {
            log.info("Opening browser using URL {}.", url);
            try {
                Desktop.getDesktop()
                    .browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                log.warn("Cannot open browser for URL {}", url, e);
            }
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping HTTP server.");
        try {
            server.stop();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot stop embedded server.", e);
        }
    }

    private WebAppContext getWebAppContext(String contextPath, String resourceRoot) {
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath(contextPath);
        Resource resource = Resource.newResource(classLoader.getResource(resourceRoot));
        webAppContext.setBaseResource(resource);
        return webAppContext;
    }

}
