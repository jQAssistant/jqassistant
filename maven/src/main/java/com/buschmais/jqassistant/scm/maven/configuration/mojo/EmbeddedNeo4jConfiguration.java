package com.buschmais.jqassistant.scm.maven.configuration.mojo;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Describes the configuration to apply to the embedded Neo4j server.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
public class EmbeddedNeo4jConfiguration {

    private Boolean connectorEnabled;

    private String listenAddress;

    private Integer boltPort;

    private Integer httpPort;

}
