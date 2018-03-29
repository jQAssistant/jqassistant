package com.buschmais.jqassistant.neo4jserver.bootstrap.api;


import com.buschmais.jqassistant.neo4jserver.bootstrap.spi.Server;

public interface ServerFactory {

    Server getServer();

}
