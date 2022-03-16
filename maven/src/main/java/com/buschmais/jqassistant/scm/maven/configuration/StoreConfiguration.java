package com.buschmais.jqassistant.scm.maven.configuration;

import java.net.URI;
import java.util.Properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents the configuration of the store.
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
public class StoreConfiguration {

    private URI uri;

    private String username;

    private String password;

    private String encryption;

    private String trustStrategy;

    private String trustCertificate;

    private Properties properties;

    private EmbeddedNeo4jConfiguration embedded = new EmbeddedNeo4jConfiguration();

}
