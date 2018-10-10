package com.buschmais.jqassistant.core.store.api;

import java.net.URI;
import java.util.Properties;

import com.buschmais.jqassistant.neo4j.backend.bootstrap.EmbeddedNeo4jConfiguration;

import lombok.*;
import lombok.Builder.Default;

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

    private String encryptionLevel;

    @Default
    private Properties properties = new Properties();

    @Default
    private EmbeddedNeo4jConfiguration embedded = EmbeddedNeo4jConfiguration.builder().build();

}
