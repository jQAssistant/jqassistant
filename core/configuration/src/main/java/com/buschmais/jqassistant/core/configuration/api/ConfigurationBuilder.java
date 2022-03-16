package com.buschmais.jqassistant.core.configuration.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
     * @param prefix
     *     The property prefix.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(String prefix, String property, String value) {
        properties.put(getKey(prefix, property), getValue(value));
        return this;
    }

    /**
     * Add a {@link Integer} property.
     *
     * @param prefix
     *     The property prefix.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(String prefix, String property, Integer value) {
        properties.put(getKey(prefix, property), getValue(value));
        return this;
    }

    /**
     * Add a boolean property.
     *
     * @param prefix
     *     The property prefix.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(String prefix, String property, boolean value) {
        properties.put(getKey(prefix, property), getValue(value));
        return this;
    }

    /**
     * Add an {@link Enum} property.
     *
     * @param prefix
     *     The property prefix.
     * @param property
     *     The name of the property.
     * @param value
     *     The value.
     * @return The {@link ConfigurationBuilder}.
     */
    public <E extends Enum<E>> ConfigurationBuilder with(String prefix, String property, E value) {
        properties.put(getKey(prefix, property), getValue(value));
        return this;
    }

    /**
     * Add a map property.
     *
     * @param prefix
     *     The property prefix.
     * @param property
     *     The name of the property.
     * @param values
     *     The values.
     * @return The {@link ConfigurationBuilder}.
     */
    public ConfigurationBuilder with(String prefix, String property, Map<String, ?> values) {
        if (values != null) {
            for (Map.Entry<String, ?> entry : values.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                addMapEntry(prefix, property, key, value);
            }
        }
        return this;
    }

    public ConfigurationBuilder with(String prefix, String property, Properties properties) {
        if (properties != null) {
            for (String key : properties.stringPropertyNames()) {
                addMapEntry(prefix, property, key, properties.getProperty(key));
            }
        }
        return this;
    }

    private void addMapEntry(String prefix, String property, String key, Object value) {
        StringBuilder keyBuilder = new StringBuilder(getKey(prefix, property)).append('.');
        if (key.contains(".")) {
            keyBuilder.append('"')
                .append(key)
                .append('"');
        } else {
            keyBuilder.append(key);
        }
        properties.put(keyBuilder.toString(), getValue(value));
    }

    /**
     * Add a list property.
     *
     * @param prefix
     *     The property prefix.
     * @param indexedProperty
     *     The name of the indexed property.
     * @param values
     *     The values.
     * @return The {@link ConfigurationBuilder}.
     */
    public <T> void with(String prefix, String indexedProperty, Iterable<T> values) {
        if (values != null) {
            int index = 0;
            for (T value : values) {
                StringBuilder key = new StringBuilder(getKey(prefix, indexedProperty)).append('[')
                    .append(index)
                    .append(']');
                properties.put(key.toString(), getValue(value));
                index++;
            }
        }
    }

    /**
     * Create a property key from a prefix and property name.
     *
     * @param prefix
     *     The prefix.
     * @param property
     *     The property name.
     * @return The property key.
     */
    private String getKey(String prefix, String property) {
        return new StringBuilder(prefix).append('.')
            .append(property)
            .toString();
    }

    /**
     * Convert a value to a string representation, currently based on {@link Object#toString()}.
     *
     * @param value
     *     The value.
     * @param <T>
     *     The value type.
     * @return The string representation.
     */
    private <T> String getValue(T value) {
        return value != null ? value.toString() : null;
    }

}
