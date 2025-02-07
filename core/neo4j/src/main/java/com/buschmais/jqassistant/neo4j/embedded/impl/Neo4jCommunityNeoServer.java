package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.takes.facets.fork.FkRegex;
import org.takes.facets.fork.TkFork;
import org.takes.http.Front;
import org.takes.http.FtBasic;

@Slf4j
class Neo4jCommunityNeoServer implements EmbeddedNeo4jServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jCommunityNeoServer.class);

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final AtomicBoolean ready = new AtomicBoolean(false);

    private Integer httpPort;

    private ClassLoader classLoader;

    private String url;

    @Override
    public void initialize(String listenAddress, Integer httpPort, Integer boltPort, ClassLoader classLoader) {
        this.httpPort = httpPort;
        this.classLoader = classLoader;
        this.url = String.format("http://%s:%d?dbms=bolt://%s:%d&preselectAuthMethod=NO_AUTH", listenAddress, httpPort, listenAddress, boltPort);
    }

    @Override
    public void start() {
        executorService.submit(() -> {
            LOGGER.info("Starting HTTP server.");
            try {
                Front front = new FtBasic(new TkFork(new FkRegex(".+", new TkClasspathResource(classLoader))), httpPort);
                front.start(ready::get);
            } catch (IOException e) {
                throw new IllegalStateException("Cannot start HTTP server.", e);
            }
            LOGGER.info("Stopped HTTP server.");
        });
        LOGGER.info("Neo4j browser available at {}.", url);
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping HTTP server.");
        ready.set(true);
        executorService.shutdown();
    }

    @Override
    public void openBrowser() {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop()
            .isSupported(Desktop.Action.BROWSE)) {
            log.info("Opening browser.");
            try {
                Desktop.getDesktop()
                    .browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                log.warn("Cannot open browser for URL {}", url, e);
            }
        }
    }

}
