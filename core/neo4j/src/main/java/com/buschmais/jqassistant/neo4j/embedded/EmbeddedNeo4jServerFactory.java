package com.buschmais.jqassistant.neo4j.embedded;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface EmbeddedNeo4jServerFactory {

    /**
     * Create {@link Properties} to configure the embedded Neo4j XO Provider
     *
     * @param connectorEnabled
     *     <code>true</code> if the Bolt connector shall be enabled.
     * @param listenAddress
     *     The listen address of the Bolt connector.
     * @param boltPort
     *     The port of the Bolt connector.
     * @param neo4jProperties
     *     Additional properties to be passed to Neo4j.
     * @param neo4jPlugins
     *     The Neo4j plugins to deploy.
     * @return The {@link Properties}
     */
    Properties getProperties(boolean connectorEnabled, String listenAddress, Integer boltPort, Map<String, String> neo4jProperties, List<File> neo4jPlugins);

    EmbeddedNeo4jServer getServer();

}
