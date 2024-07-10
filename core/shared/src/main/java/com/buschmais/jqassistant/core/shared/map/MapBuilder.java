package com.buschmais.jqassistant.core.shared.map;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

/**
 * Utility class for building maps using a fluent API.
 *
 * @param <K>
 *            The key type.
 * @param <V>
 *            The value Type.
 */
public class MapBuilder<K, V> {

    private Map<K, V> map = new HashMap<>();

    /**
     * Private constructor.
     */
    protected MapBuilder() {
    }

    /**
     * Create a map builder instance.
     *
     * @return The map builder instance.
     */
    public static <K, V> MapBuilder<K, V> builder() {
        return new MapBuilder<>();
    }

    /**
     * Add an entry.
     *
     * @param key
     *            The key.
     * @param value
     *            The value.
     * @return The map builder.
     */
    public MapBuilder<K, V> entry(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Return the map.
     *
     * @return The map.
     */
    public Map<K, V> build() {
        return unmodifiableMap(map);
    }
}
