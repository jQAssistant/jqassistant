package com.buschmais.jqassistant.neo4j.embedded;

import java.io.File;
import java.util.List;
import java.util.Properties;

public interface EmbeddedNeo4jServerFactory {

    Properties getProperties(boolean connectorEnabled, String listenAddress, Integer boltPort, List<File> neo4jPlugins);

    EmbeddedNeo4jServer getServer();

}
