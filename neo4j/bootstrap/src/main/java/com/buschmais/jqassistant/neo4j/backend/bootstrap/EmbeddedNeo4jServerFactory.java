package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import com.buschmais.xo.api.bootstrap.XOUnit;

public interface EmbeddedNeo4jServerFactory {

    void configure(XOUnit.XOUnitBuilder builder);

    EmbeddedNeo4jServer getServer();

}
