package com.buschmais.jqassistant.neo4j.backend.neo4jv2;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.AbstractEmbeddedNeo4jServer;

import org.apache.commons.io.FileUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseAPI;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Level;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.configuration.ServerSettings;
import org.neo4j.server.database.Database;
import org.neo4j.server.database.WrappedDatabase;

public class Neo4jV2CommunityNeoServer extends AbstractEmbeddedNeo4jServer {

    private Path tempDirectory;

    private CommunityNeoServer communityNeoServer;

    @Override
    protected void configure(GraphDatabaseService graphDatabaseService, boolean apocEnabled) {
    }

    @Override
    public String getVersion() {
        return "2.x";
    }

    @Override
    public void start(String httpAddress, int httpPort) {
        tempDirectory = createTempDirectory();
        Map<String, String> opts = new HashMap<>();
        // Neo4j 2.x
        opts.put("dbms.security.auth_enabled", Boolean.FALSE.toString());
        opts.put("org.neo4j.server.webserver.address", httpAddress);
        opts.put("org.neo4j.server.webserver.port", Integer.toString(httpPort));
        // Neo4j 2.x/3.x
        String sslDir = tempDirectory.toFile().getAbsolutePath() + "neo4j-home/";
        opts.put(ServerSettings.tls_key_file.name(), sslDir + "/ssl/snakeoil.key");
        opts.put(ServerSettings.tls_certificate_file.name(), sslDir + "/ssl/snakeoil.cert");

        Config defaults = new Config(opts); // Config.empty().with(opts);
        FormattedLogProvider logProvider = FormattedLogProvider.withDefaultLogLevel(Level.INFO).toOutputStream(System.out);
        GraphDatabaseDependencies graphDatabaseDependencies = GraphDatabaseDependencies.newDependencies().userLogProvider(logProvider);
        Database.Factory factory = new Database.Factory() {
            @Override
            public Database newDatabase(Config config, GraphDatabaseFacadeFactory.Dependencies dependencies) {
                return new WrappedDatabase((GraphDatabaseAPI) graphDatabaseService);
            }
        };
        communityNeoServer = new CommunityNeoServer(defaults, factory, graphDatabaseDependencies, logProvider);
        communityNeoServer.start();
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
        communityNeoServer.stop();
        try {
            FileUtils.deleteDirectory(tempDirectory.toFile());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot delete temp directory.", e);
        }
    }
}
