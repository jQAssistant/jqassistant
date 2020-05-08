package com.buschmais.jqassistant.core.store.api;

import java.net.URI;
import java.util.Properties;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import static lombok.AccessLevel.PRIVATE;

/**
 * Represents the configuration of the store.
 */
@Setter
@Getter
@ToString
@Builder
@AllArgsConstructor(access = PRIVATE)
@NoArgsConstructor
public class StoreConfiguration {

    private URI uri;

    private String username;

    private String password;

    @Builder.Default
    private String encryption = "false";

    private String trustStrategy;

    private String trustCertificate;

    @Default
    private Properties properties = new Properties();

    @Default
    private EmbeddedNeo4jConfiguration embedded = EmbeddedNeo4jConfiguration.builder().build();

}
