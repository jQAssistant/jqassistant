package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Properties;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.configuration.Embedded;

public interface EmbeddedNeo4jServerFactory {

    Properties getProperties(Embedded embedded);

    EmbeddedNeo4jServer getServer();

}
