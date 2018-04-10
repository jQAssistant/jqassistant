package com.buschmais.jqassistant.neo4j.backend.neo4jv3;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.AbstractEmbeddedNeo4jServer;

import apoc.coll.Coll;
import apoc.convert.Json;
import apoc.create.Create;
import apoc.help.Help;
import apoc.index.FulltextIndex;
import apoc.load.LoadJson;
import apoc.load.Xml;
import apoc.map.Maps;
import apoc.meta.Meta;
import apoc.path.PathExplorer;
import apoc.refactor.GraphRefactoring;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.kernel.GraphDatabaseDependencies;
import org.neo4j.kernel.api.exceptions.KernelException;
import org.neo4j.kernel.configuration.Config;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacade;
import org.neo4j.kernel.impl.factory.GraphDatabaseFacadeFactory;
import org.neo4j.kernel.impl.proc.Procedures;
import org.neo4j.kernel.internal.GraphDatabaseAPI;
import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Level;
import org.neo4j.server.CommunityNeoServer;
import org.neo4j.server.database.Database;
import org.neo4j.server.database.WrappedDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;

public class Neo4jV3CommunityNeoServer extends AbstractEmbeddedNeo4jServer {


    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jV3CommunityNeoServer.class);

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
        Procedures procedures = ((GraphDatabaseAPI) graphDatabaseService).getDependencyResolver().resolveDependency(Procedures.class);
        for (Class<?> procedureClass : asList(Coll.class, Maps.class, Json.class, Create.class, apoc.date.Date.class, FulltextIndex.class, apoc.lock.Lock.class, LoadJson.class,
            Xml.class, PathExplorer.class, Meta.class, GraphRefactoring.class, Help.class)) {
            try {
                LOGGER.debug("Registering procedure class " + procedureClass.getName());
                procedures.registerProcedure(procedureClass);
            } catch (KernelException e) {
                LOGGER.warn("Cannot register procedure " + procedureClass.getName(), e);
            }
        }
    }
}
