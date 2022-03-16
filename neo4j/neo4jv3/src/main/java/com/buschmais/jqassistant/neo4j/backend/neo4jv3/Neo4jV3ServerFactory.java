package com.buschmais.jqassistant.neo4j.backend.neo4jv3;

import java.util.Properties;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.AbstractEmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.configuration.Embedded;

public class Neo4jV3ServerFactory extends AbstractEmbeddedNeo4jServerFactory {

    private static final String PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED = "neo4j.dbms.connector.bolt.enabled";
    private static final String PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_LISTEN_ADDRESS = "neo4j.dbms.connector.bolt.listen_address";
    private static final String PROPERTY_NEO4J_DBMS_SECURITY_PROCEDURES_UNRESTRICTED = "neo4j.dbms.security.procedures.unrestricted";
    private static final String PROPERTY_NEO4J_DBMS_TX_LOG_ROTATION_SIZE = "neo4j.dbms.tx_log.rotation.size";
    private static final String PROPERTY_NEO4J_DBMS_TX_LOG_ROTATION_RETENTION_POLICY = "neo4j.dbms.tx_log.rotation.retention_policy";

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jV3CommunityNeoServer();
    }

    @Override
    public Properties getProperties(Embedded embedded) {
        Properties commonProperties = getCommonProperties();
        commonProperties.putIfAbsent(PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED, Boolean.toString(embedded.connectorEnabled()));
        commonProperties.putIfAbsent(PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_LISTEN_ADDRESS, embedded.listenAddress() + ":" + embedded.boltPort());
        commonProperties.putIfAbsent(PROPERTY_NEO4J_DBMS_SECURITY_PROCEDURES_UNRESTRICTED, "*");
        commonProperties.putIfAbsent(PROPERTY_NEO4J_DBMS_TX_LOG_ROTATION_SIZE, "50M");
        commonProperties.putIfAbsent(PROPERTY_NEO4J_DBMS_TX_LOG_ROTATION_RETENTION_POLICY, Boolean.FALSE.toString());
        return commonProperties;
    }
}
