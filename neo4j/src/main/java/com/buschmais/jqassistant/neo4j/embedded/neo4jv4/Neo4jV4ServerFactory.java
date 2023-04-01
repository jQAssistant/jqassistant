package com.buschmais.jqassistant.neo4j.embedded.neo4jv4;

import java.util.Properties;

import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServer;
import com.buschmais.jqassistant.neo4j.embedded.EmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.embedded.configuration.Embedded;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public class Neo4jV4ServerFactory implements EmbeddedNeo4jServerFactory {

    private static final String ALLOW_STORE_UPGRADE = "neo4j.allow_store_upgrade";
    private static final String KEEP_LOGICAL_LOGS = "neo4j.keep_logical_logs";

    private static final String DBMS_ALLOW_FORMAT_MIGRATION = "neo4j.dbms.allow_format_migration";
    private static final String DBMS_SECURITY_PROCEDURES_UNRESTRICTED = "neo4j.dbms.security.procedures.unrestricted";
    private static final String DBMS_TX_LOG_ROTATION_SIZE = "neo4j.dbms.tx_log.rotation.size";
    private static final String DBMS_TX_LOG_ROTATION_RETENTION_POLICY = "neo4j.dbms.tx_log.rotation.retention_policy";

    private static final String DBMS_CONNECTOR_BOLT_ENABLED = "neo4j.dbms.connector.bolt.enabled";

    private static final String DBMS_CONNECTOR_BOLT_LISTEN_ADDRESS = "neo4j.dbms.connector.bolt.listen_address";

    private static final String UNSUPPORTED_DUMP_DIAGNOSTICS = "neo4j.unsupported.dbms.dump_diagnostics";

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jV4CommunityNeoServer();
    }

    @Override
    public Properties getProperties(Embedded embedded) {
        Properties properties = new Properties();
        properties.setProperty(ALLOW_STORE_UPGRADE, TRUE.toString());
        properties.setProperty(KEEP_LOGICAL_LOGS, FALSE.toString());
        properties.setProperty(DBMS_ALLOW_FORMAT_MIGRATION, TRUE.toString());
        properties.setProperty(DBMS_SECURITY_PROCEDURES_UNRESTRICTED, "*");
        properties.setProperty(DBMS_TX_LOG_ROTATION_SIZE, "50M");
        properties.setProperty(DBMS_TX_LOG_ROTATION_RETENTION_POLICY, FALSE.toString());
        properties.setProperty(UNSUPPORTED_DUMP_DIAGNOSTICS, FALSE.toString());
        if (embedded.connectorEnabled()){
            properties.setProperty(DBMS_CONNECTOR_BOLT_ENABLED, TRUE.toString());
            properties.setProperty(DBMS_CONNECTOR_BOLT_LISTEN_ADDRESS, embedded.listenAddress() + ":" + embedded.boltPort());
        }
        return properties;
    }
}
