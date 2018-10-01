package com.buschmais.jqassistant.neo4j.backend.neo4jv3;

import java.util.Properties;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.AbstractEmbeddedNeo4jServerFactory;
import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jServer;

public class Neo4jV3ServerFactory extends AbstractEmbeddedNeo4jServerFactory {

    private static final String PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED = "neo4j.dbms.connector.bolt.enabled";
    private static final String PROPERTY_NEO4J_DBMS_SECURITY_PROCEDURES_UNRESTRICTED = "neo4j.dbms.security.procedures.unrestricted";

    @Override
    public EmbeddedNeo4jServer getServer() {
        return new Neo4jV3CommunityNeoServer();
    }

    @Override
    protected void addXOUnitProperties(Properties xoUnitProperties) {
        xoUnitProperties.putIfAbsent(PROPERTY_NEO4J_DBMS_CONNECTOR_BOLT_ENABLED, Boolean.TRUE.toString());
        xoUnitProperties.putIfAbsent(PROPERTY_NEO4J_DBMS_SECURITY_PROCEDURES_UNRESTRICTED, "apoc.*");
    }
}
