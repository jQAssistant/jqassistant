package com.buschmais.jqassistant.neo4j.embedded;

import java.io.File;
import java.util.Optional;
import java.util.Properties;

public interface EmbeddedNeo4jServerFactory {

    Properties getProperties(boolean connectorEnabled, String listenAddress, Integer boltPort, Optional<File> pluginDirectory);

    EmbeddedNeo4jServer getServer();

}
