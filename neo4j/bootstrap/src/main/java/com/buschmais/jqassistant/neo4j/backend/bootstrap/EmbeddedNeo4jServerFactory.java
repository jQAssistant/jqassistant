package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import java.util.Properties;

import com.buschmais.xo.api.bootstrap.XOUnit;

public interface EmbeddedNeo4jServerFactory {

    void configure(XOUnit.XOUnitBuilder builder, Properties properties);

    EmbeddedNeo4jServer getServer();

}
