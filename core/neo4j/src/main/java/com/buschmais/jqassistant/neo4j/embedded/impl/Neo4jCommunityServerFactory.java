package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.io.File;
import java.util.List;
import java.util.Properties;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServerFactory;
import com.buschmais.xo.neo4j.embedded.api.EmbeddedNeo4jXOProvider;

import lombok.extern.slf4j.Slf4j;
import org.neo4j.configuration.GraphDatabaseInternalSettings;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.io.ByteUnit;

import static java.lang.Boolean.FALSE;
import static java.util.stream.Collectors.joining;

@Slf4j
public class Neo4jCommunityServerFactory implements EmbeddedNeo4jServerFactory {

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jCommunityNeoServer();
    }

    @Override
    public Properties getProperties(boolean connectorEnabled, String listenAddress, Integer boltPort, List<File> plugins) {
        EmbeddedNeo4jXOProvider.PropertiesBuilder propertiesBuilder = EmbeddedNeo4jXOProvider.propertiesBuilder()
            .property(GraphDatabaseSettings.procedure_unrestricted, List.of("*"))
            // keep disk footprint small (TX logs)
            .property(GraphDatabaseSettings.keep_logical_logs, FALSE.toString())
            .property(GraphDatabaseSettings.logical_log_rotation_threshold, ByteUnit.mebiBytes(25L))
            // deactivate unnecessary logging
            .property(GraphDatabaseSettings.log_queries, GraphDatabaseSettings.LogQueryLevel.OFF)
            .property(GraphDatabaseInternalSettings.dump_diagnostics, false)
            // don't wait on server shutdown
            .property(GraphDatabaseInternalSettings.netty_server_shutdown_quiet_period, 0);
        if (connectorEnabled) {
            propertiesBuilder.property(BoltConnector.enabled, true);
            propertiesBuilder.property(BoltConnector.listen_address, new SocketAddress(listenAddress, boltPort));
        }
        Properties properties = propertiesBuilder.build();
        if (!plugins.isEmpty()) {
            properties.setProperty(EmbeddedNeo4jXOProvider.PROPERTY_XO_NEO4J_EMBEDDED_PLUGINS, plugins.stream()
                .map(File::getAbsolutePath)
                .collect(joining(",")));
        }
        // set string properties which are not available for Neo4j v4
        // deactivate internal debug logs
        properties.setProperty("neo4j.server.logs.debug.enabled", FALSE.toString());
        // deactivate user data collector
        properties.setProperty("neo4j.dbms.usage_report.enabled", FALSE.toString());
        return properties;
    }

}
