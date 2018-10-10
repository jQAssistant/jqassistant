package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Properties;

public interface EmbeddedNeo4jServerFactory {

    Properties getProperties(EmbeddedNeo4jConfiguration embedded);

    EmbeddedNeo4jServer getServer();

}
