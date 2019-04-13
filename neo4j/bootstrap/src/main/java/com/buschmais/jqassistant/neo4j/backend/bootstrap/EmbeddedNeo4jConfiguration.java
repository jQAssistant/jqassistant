package com.buschmais.jqassistant.neo4j.backend.bootstrap;

import lombok.*;
import lombok.Builder.Default;

/**
 * Describes the configuration to apply to the embedded Neo4j server.
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class EmbeddedNeo4jConfiguration {

    public static final String DEFAULT_LISTEN_ADDRESS = "localhost";
    public static final int DEFAULT_BOLT_PORT = 7687;
    public static final int DEFAULT_HTTP_PORT = 7474;
    public static final boolean DEFAULT_APOC_ENABLED = false;
    public static final boolean DEFAULT_GRAPH_ALGORITHMS_ENABLED = false;

    @Default
    private boolean connectorEnabled = false;

    @Default
    private String listenAddress = DEFAULT_LISTEN_ADDRESS;

    @Default
    private Integer boltPort = DEFAULT_BOLT_PORT;

    @Default
    private Integer httpPort = DEFAULT_HTTP_PORT;

    @Default
    boolean apocEnabled = DEFAULT_APOC_ENABLED;

    @Default
    boolean graphAlgorithmsEnabled = DEFAULT_GRAPH_ALGORITHMS_ENABLED;
}
