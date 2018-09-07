package com.buschmais.jqassistant.core.store.api;

import java.net.URI;
import java.util.Properties;

import lombok.*;

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

    private Properties properties;

    @Builder.Default
    boolean apocEnabled = true;
}
