package com.buschmais.jqassistant.neo4j.embedded.neo4jv4;

import java.util.List;
import java.util.Properties;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.embedded.configuration.Embedded;

import org.neo4j.configuration.GraphDatabaseInternalSettings;
import org.neo4j.configuration.GraphDatabaseSettings;
import org.neo4j.configuration.connectors.BoltConnector;
import org.neo4j.configuration.helpers.SocketAddress;
import org.neo4j.graphdb.config.Setting;
import org.neo4j.io.ByteUnit;

import static java.lang.Boolean.FALSE;

public class Neo4jV4ServerFactory implements EmbeddedNeo4jServerFactory {

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jV4CommunityNeoServer();
    }

    @Override
    public Properties getProperties(Embedded embedded) {
        Neo4jPropertiesBuilder builder = Neo4jPropertiesBuilder.builder();
        builder.property(GraphDatabaseSettings.allow_upgrade, true);
        builder.property(GraphDatabaseSettings.keep_logical_logs, FALSE.toString());
        builder.property(GraphDatabaseSettings.logical_log_rotation_threshold, ByteUnit.mebiBytes(25L));
        builder.property(GraphDatabaseSettings.procedure_unrestricted, List.of("*"));
        builder.property(GraphDatabaseInternalSettings.dump_diagnostics, false);
        if (embedded.connectorEnabled()){
            builder.property(BoltConnector.enabled, true);
            builder.property(BoltConnector.listen_address, new SocketAddress(embedded.listenAddress(), embedded.boltPort()));
        }
        return builder.build();
    }

    private static class Neo4jPropertiesBuilder {

        private static final String NEO4J_PROPERTY_PREFIX = "neo4j.";

        private Properties properties = new Properties();

        static Neo4jPropertiesBuilder builder() {
            return new Neo4jPropertiesBuilder();
        }

        <T> Neo4jPropertiesBuilder property(Setting<T> setting, T value) {
            properties.setProperty(NEO4J_PROPERTY_PREFIX + setting.name(), value.toString());
            return this;
        }

        Properties build() {
            return properties;
        }
    }
}
