package com.buschmais.jqassistant.neo4j.embedded.impl;

import java.util.Map;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class Neo4jCommunityServerFactoryTest {

    private final Neo4jCommunityServerFactory serverFactory = new Neo4jCommunityServerFactory();

    @Test
    void properties() {
        Properties properties = serverFactory.getProperties(true, "localhost", 7687,
            Map.of("dbms.memory.transaction.total.max", "8G", "db.tx_log.rotation.size", "1M"), emptyList());
        switch (Runtime.version()
            .feature()) {
        case 11:
            // Neo4j 4x
            assertThat(properties).containsEntry("neo4j.dbms.connector.bolt.enabled", "true");
            assertThat(properties).containsEntry("neo4j.dbms.connector.bolt.listen_address", "localhost:7687");
            break;
        default:
            // Neo4j 5x
            assertThat(properties).containsEntry("neo4j.server.bolt.enabled", "true");
            assertThat(properties).containsEntry("neo4j.server.bolt.listen_address", "localhost:7687");
            break;
        }
        assertThat(properties).containsEntry("neo4j.dbms.memory.transaction.total.max", "8G");
        // overrides default value
        assertThat(properties).containsEntry("neo4j.db.tx_log.rotation.size", "1M");
    }

}
