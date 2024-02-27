package com.buschmais.jqassistant.core.store.api.configuration;

import java.io.File;
import java.util.List;
import java.util.Optional;

import com.buschmais.jqassistant.core.shared.configuration.Plugin;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

@ConfigMapping(prefix = "jqassistant.store.embedded")
public interface Embedded {

    String DEFAULT_LISTEN_ADDRESS = "localhost";
    String DEFAULT_BOLT_PORT = "7687";
    String DEFAULT_HTTP_PORT = "7474";

    String CONNECTOR_ENABLED = "connector-enabled";

    @WithDefault("false")
    boolean connectorEnabled();

    String LISTEN_ADDRESS = "listen-address";

    @WithDefault(DEFAULT_LISTEN_ADDRESS)
    String listenAddress();

    String BOLT_PORT = "bolt-port";

    @WithDefault(DEFAULT_BOLT_PORT)
    Integer boltPort();

    String HTTP_PORT = "http-port";

    @WithDefault(DEFAULT_HTTP_PORT)
    Integer httpPort();

    String NEO4J_PLUGINS = "neo4j-plugins";

    List<Plugin> neo4jPlugins();

    String NEO4J_PLUGIN_DIRECTORY = "neo4j-plugin-directory";
    Optional<File> neo4jPluginDirectory();
}
