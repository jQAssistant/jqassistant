package com.buschmais.jqassistant.neo4jserver.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Level;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.configuration.ServerSettings;
import org.neo4j.server.database.Database;

import com.buschmais.jqassistant.neo4jserver.api.Server;

public class ExtendedCommunityNeoServer implements Server {

    public static final String DEFAULT_ADDRESS = "localhost";

    public static final int DEFAULT_PORT = 7474;

    private GraphDatabaseService databaseService;

    private String httpAddress;
    private int httpPort;

    private String boltAddress = DEFAULT_ADDRESS;
    private int boltPort = 7687;

    private Path tempDirectory;

    public ExtendedCommunityNeoServer(EmbeddedGraphStore store, String address, int port) {
        this.databaseService = store.getGraphDatabaseService();
        this.httpAddress = address;
        this.httpPort = port;
    }

    @Override
    public void start() {
        tempDirectory = createTempDirectory();
        Database.Factory factory = new Database.Factory() {
            @Override
            public Database newDatabase(Config config, GraphDatabaseFacadeFactory.Dependencies dependencies) {
                return new EmbeddedDatabase();
            }
        };
        Map<String, String> opts = new HashMap<>();
        // Neo4j 2.x
        opts.put("dbms.security.auth_enabled", Boolean.FALSE.toString());
        opts.put("org.neo4j.server.webserver.address", httpAddress);
        opts.put("org.neo4j.server.webserver.port", Integer.toString(httpPort));
        // Neo4j 3.x
        opts.put("dbms.connector.http.type", "HTTP");
        opts.put("dbms.connector.http.enabled", "true");
        opts.put("dbms.connector.http.listen_address", httpAddress + ":" + httpPort);

        // Neo4j 2.x/3.x
        String sslDir = tempDirectory.toFile().getAbsolutePath() + "neo4j-home/";
        opts.put(ServerSettings.tls_key_file.name(), sslDir + "/ssl/snakeoil.key");
        opts.put(ServerSettings.tls_certificate_file.name(), sslDir + "/ssl/snakeoil.cert");

        Config defaults = new Config(opts); // Config.empty().with(opts);
        FormattedLogProvider logProvider = FormattedLogProvider.withDefaultLogLevel(Level.INFO).toOutputStream(System.out);
        GraphDatabaseDependencies graphDatabaseDependencies = GraphDatabaseDependencies.newDependencies().userLogProvider(logProvider);
        CommunityNeoServer server = new CommunityNeoServer(defaults, factory, graphDatabaseDependencies, logProvider);
        server.start();
    }

    private Path createTempDirectory() {
        try {
            return Files.createTempDirectory("neo4j-home");
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create temp directory.", e);
        }
    }

    @Override
    public void stop() {
        try {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot delete temp directory.", e);
        }
    }

    private class EmbeddedDatabase implements org.neo4j.server.database.Database {
        @Override
        public String getLocation() {
            return "embedded";
        }

        @Override
        public GraphDatabaseFacade getGraph() {
            return (GraphDatabaseFacade) databaseService;
        }

        @Override
        public boolean isRunning() {
            return true;
        }

        @Override
        public void init() throws Throwable {

        }

        @Override
        public void start() throws Throwable {

        }

        @Override
        public void stop() throws Throwable {

        }

        @Override
        public void shutdown() throws Throwable {
        }
    }
}
