package com.buschmais.jqassistant.neo4j.embedded;

import java.util.Properties;

import com.buschmais.jqassistant.neo4j.embedded.configuration.Embedded;

public interface EmbeddedNeo4jServerFactory {

    Properties getProperties(Embedded embedded);

    EmbeddedNeo4jServer getServer();

}
