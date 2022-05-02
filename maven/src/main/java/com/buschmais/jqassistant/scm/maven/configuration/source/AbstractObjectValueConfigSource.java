package com.buschmais.jqassistant.scm.maven.configuration.source;

import java.util.*;

import io.smallrye.config.common.AbstractConfigSource;
import org.codehaus.plexus.interpolation.PrefixedObjectValueSource;

/**
 * Abstract config source wrapping a Maven object providing properties values which are extracted using a {@link PrefixedObjectValueSource}.
 *
 * @param <T>
 *     The type of the Maven object.
 */
class AbstractObjectValueConfigSource<T> extends AbstractConfigSource {

    private final PrefixedObjectValueSource valueSource;
    private final Set<String> propertyNames;

    private final Map<String, String> properties = new HashMap<>();

    AbstractObjectValueConfigSource(String name, T valueObject, String prefix, Collection<String> propertyNames) {
        super(name, DEFAULT_ORDINAL);
        this.valueSource = new PrefixedObjectValueSource(prefix, valueObject);
        this.propertyNames = new HashSet<>(propertyNames);
    }

    @Override
    public final Set<String> getPropertyNames() {
        return propertyNames;
    }

    @Override
    public String getValue(String property) {
        return properties.computeIfAbsent(property, key -> {
            Object value = valueSource.getValue(key);
            return value != null ?
                value.toString()
                    .replace("\\", "\\\\") :
                null;
        });
    }
}
