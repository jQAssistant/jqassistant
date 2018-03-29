package com.buschmais.jqassistant.neo4jserver.neo4jv3;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.neo4jserver.bootstrap.spi.AbstractServer;

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

public class Neo4jV3CommunityNeoServer extends AbstractServer {

    private CommunityNeoServer communityNeoServer;

    @Override
    public void start(String httpAddress, int httpPort) {
        Map<String, String> opts = new HashMap<>();
        // Neo4j 3.x
        opts.put("dbms.connector.http.type", "HTTP");
        opts.put("dbms.connector.http.enabled", "true");
        opts.put("dbms.connector.http.listen_address", httpAddress + ":" + httpPort);

        Config defaults = Config.defaults(opts);
        FormattedLogProvider logProvider = FormattedLogProvider.withDefaultLogLevel(Level.INFO).toOutputStream(System.out);
        final GraphDatabaseDependencies graphDatabaseDependencies = GraphDatabaseDependencies.newDependencies().userLogProvider(logProvider);
        Database.Factory factory = new Database.Factory() {
            @Override
            public Database newDatabase(Config config, GraphDatabaseFacadeFactory.Dependencies dependencies) {
                return new WrappedDatabase((GraphDatabaseFacade) graphDatabaseService);
            }
        };
        communityNeoServer = new CommunityNeoServer(defaults, factory, graphDatabaseDependencies, logProvider);
        communityNeoServer.start();
    }

    @Override
    public void stop() {
        communityNeoServer.stop();
    }

    @Override
    protected void configure(GraphDatabaseService graphDatabaseService) {
    }
}
