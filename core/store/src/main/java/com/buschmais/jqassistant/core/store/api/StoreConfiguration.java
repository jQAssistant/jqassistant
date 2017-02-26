package com.buschmais.jqassistant.core.store.api;

import static lombok.AccessLevel.PRIVATE;

import java.net.URI;
import java.util.Properties;

import lombok.*;

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

    private Properties properties;
}
