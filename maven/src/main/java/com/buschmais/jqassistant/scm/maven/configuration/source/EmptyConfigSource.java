package com.buschmais.jqassistant.scm.maven.configuration.source;

import java.util.Set;

import lombok.NoArgsConstructor;
import org.eclipse.microprofile.config.spi.ConfigSource;

import static java.util.Collections.emptySet;
import static lombok.AccessLevel.PRIVATE;

/**
 * Empty config source which provides no properties.
 */
@NoArgsConstructor(access = PRIVATE)
public final class EmptyConfigSource implements ConfigSource {

    public static final EmptyConfigSource INSTANCE = new EmptyConfigSource();

    @Override
    public Set<String> getPropertyNames() {
        return emptySet();
    }

    @Override
    public String getValue(String s) {
        return null;
    }

    @Override
    public String getName() {
        return "Empty";
    }
}
