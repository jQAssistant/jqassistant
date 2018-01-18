package com.buschmais.jqassistant.neo4jserver.neo4jv3;

import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Level;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.database.Database;
import org.neo4j.server.database.WrappedDatabase;

import com.buschmais.jqassistant.core.store.impl.EmbeddedGraphStore;
import com.buschmais.jqassistant.neo4jserver.bootstrap.api.Server;

public class Neo4jV3CommunityNeoServer implements Server {

    private GraphDatabaseService databaseService;

    private String httpAddress;
    private int httpPort;

    private CommunityNeoServer communityNeoServer;

    public Neo4jV3CommunityNeoServer(EmbeddedGraphStore store, String address, int port) {
        this.databaseService = store.getGraphDatabaseService();
        this.httpAddress = address;
        this.httpPort = port;
    }

    @Override
    public void start() {
        Map<String, String> opts = new HashMap<>();
        // Neo4j 3.x
        opts.put("dbms.connector.http.type", "HTTP");
        opts.put("dbms.connector.http.enabled", "true");
        opts.put("dbms.connector.http.listen_address", httpAddress + ":" + httpPort);

        Config defaults = Config.defaults(opts);
        FormattedLogProvider logProvider = FormattedLogProvider.withDefaultLogLevel(Level.INFO).toOutputStream(System.out);
        GraphDatabaseDependencies graphDatabaseDependencies = GraphDatabaseDependencies.newDependencies().userLogProvider(logProvider);
        Database.Factory factory = new Database.Factory() {
            @Override
            public Database newDatabase(Config config, GraphDatabaseFacadeFactory.Dependencies dependencies) {
                return new WrappedDatabase((GraphDatabaseFacade) databaseService);
            }
        };
        communityNeoServer = new CommunityNeoServer(defaults, factory, graphDatabaseDependencies, logProvider);
        communityNeoServer.start();
    }

    @Override
    public void stop() {
        communityNeoServer.stop();
    }
}
