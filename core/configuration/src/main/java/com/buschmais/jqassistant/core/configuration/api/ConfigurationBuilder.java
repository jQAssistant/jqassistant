package com.buschmais.jqassistant.core.configuration.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.PropertiesConfigSource;
import org.eclipse.microprofile.config.spi.ConfigSource;

/**
 * Builder for a {@link ConfigSource} providing utility methods to support construction.
 * <p>
 * The methods for adding properties take the following parameters:
 * <p>
 * - prefix: The prefix to prepend for the property name, e.g. "jqassistant.scan"
 * - property: The property name, will be appended to the prefix, e.g. "continue-on-error" will create a property jqassistant.scan.continue-on-error"
 * - value: The property value to set, will be converted to a string representation.
 */
public class ConfigurationBuilder {

    private final String name;

    private final int ordinal;

    private final Map<String, String> properties = new HashMap<>();

    /**
     * Constructor.
     *
     * @param name
     *     The name of the {@link ConfigSource}.
     * @param ordinal
     *     The ordinal of the {@link ConfigSource}.
     */
    public ConfigurationBuilder(String name, int ordinal) {
        this.name = name;
        this.ordinal = ordinal;
    }

    /**
     * Build the {@link ConfigSource}.
     *
     * @return The {@link ConfigSource}.
     */
    public ConfigSource build() {
        return new PropertiesConfigSource(properties, name, ordinal);
    }

    /**
     * Add a {@link String} property.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(Class<?> mapping, String property, String value) {
        put(mapping, property, value);
        return this;
    }

    /**
     * Add a {@link URI} property.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(Class<?> mapping, String property, URI value) {
        put(mapping, property, value);
        return this;
    }

    /**
     * Add a {@link Integer} property.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(Class<?> mapping, String property, Integer value) {
        put(mapping, property, value);
        return this;
    }

    /**
     * Add a boolean property.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(Class<?> mapping, String property, Boolean value) {
        put(mapping, property, value);
        return this;
    }

    /**
     * Add an {@link Enum} property.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public <E extends Enum<E>> ConfigurationBuilder with(Class<?> mapping, String property, E value) {
        put(mapping, property, value);
        return this;
    }

    /**
     * Add a map property.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The name of the property.
     * @param values
     *     The values.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(Class<?> mapping, String property, Map<String, ?> values) {
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                addMapEntry(mapping, property, key, value);
            }
        }
        return this;
    }

    /**
     * Add properties property.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The name of the property.
     * @param properties
     *     The properties.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(Class<?> mapping, String property, Properties properties) {
        if (properties != null) {
            for (String key : properties.stringPropertyNames()) {
                addMapEntry(mapping, property, key, properties.getProperty(key));
            }
        }
        return this;
    }

    private void addMapEntry(Class<?> mapping, String property, String key, Object value) {
        StringBuilder keyBuilder = new StringBuilder(getKey(mapping, property)).append('.');
        if (key.contains(".")) {
            keyBuilder.append('"')
                .append(key)
                .append('"');
        } else {
            keyBuilder.append(key);
        }
        put(keyBuilder.toString(), value);
    }

    /**
     * Add a list property.
     *
     * @param mapping
     *     The mapping.
     * @param indexedProperty
     *     The name of the indexed property.
     * @param values
     *     The values.
     * @return The {@link ConfigurationBuilder}.
     */
    public <T> void with(Class<?> mapping, String indexedProperty, Iterable<T> values) {
        if (values != null) {
            int index = 0;
            for (T value : values) {
                StringBuilder key = new StringBuilder(getKey(mapping, indexedProperty)).append('[')
                    .append(index)
                    .append(']');
                put(key.toString(), value);
                index++;
            }
        }
    }

    /**
     * Create a property key from a prefix and property name.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The property name.
     * @return The property key.
     */
    private String getKey(Class<?> mapping, String property) {
        ConfigMapping configMapping = mapping.getAnnotation(ConfigMapping.class);
        if (configMapping == null) {
            throw new IllegalArgumentException("Class " + mapping.getName() + " is not annotated with " + ConfigMapping.class.getName());
        }
        return new StringBuilder(configMapping.prefix()).append('.')
            .append(property)
            .toString();
    }

    /**
     * Put a non-null value for the given mapping into the configuration properties.
     *
     * @param mapping
     *     The mapping.
     * @param property
     *     The property name.
     * @param value
     *     The value.
     * @param <T>
     *     The value type.
     */
    private <T> void put(Class<?> mapping, String property, T value) {
        put(getKey(mapping, property), value);
    }

    /**
     * Put a non-null value for the given key into the configuration properties.
     *
     * @param key
     *     The mapping.
     * @param value
     *     The value.
     * @param <T>
     *     The value type.
     */
    private <T> void put(String key, T value) {
        if (value != null) {
            properties.put(key, value.toString());
        }
    }
}
