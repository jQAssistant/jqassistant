package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Properties;

/**
 * Abstract base class for {@link EmbeddedNeo4jServerFactory}s.
 */
public abstract class AbstractEmbeddedNeo4jServerFactory implements EmbeddedNeo4jServerFactory {

    private static final String PROPERTY_NEO4J_ALLOW_STORE_UPGRADE = "neo4j.allow_store_upgrade";
    private static final String PROPERTY_NEO4J_KEEP_LOGICAL_LOGS = "neo4j.keep_logical_logs";
    private static final String PROPERTY_NEO4J_DBMS_ALLOW_FORMAT_MIGRATION = "neo4j.dbms.allow_format_migration";

    protected final Properties getCommonProperties() {
        Properties properties = new Properties();
        properties.setProperty(PROPERTY_NEO4J_ALLOW_STORE_UPGRADE, Boolean.TRUE.toString());
        properties.setProperty(PROPERTY_NEO4J_KEEP_LOGICAL_LOGS, Boolean.FALSE.toString());
        properties.setProperty(PROPERTY_NEO4J_DBMS_ALLOW_FORMAT_MIGRATION, Boolean.TRUE.toString());
        return properties;
    }

}
