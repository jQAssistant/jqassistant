package com.buschmais.jqassistant.plugin.common.test.scanner;

import java.util.HashMap;
import java.util.Map;

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
    private MapBuilder() {
    }

    /**
     * Create a map builder instance.
     * 
     * @return The map builder instance.
     */
    public static <K, V> MapBuilder<K, V> create() {
        return new MapBuilder<>();
    }

    /**
     * Create a map builder instance using an initial key/value pair.
     *
     * @param key
     *            The key.
     * @param value
     *            The value.
     * @return The map builder instance.
     */
    public static <K, V> MapBuilder<K, V> create(K key, V value) {
        MapBuilder<K, V> builder = create();
        builder.put(key, value);
        return builder;
    }

    /**
     * Put a key/value pair.
     * 
     * @param key
     *            The key.
     * @param value
     *            The value.
     * @return The map builder.
     */
    public MapBuilder<K, V> put(K key, V value) {
        map.put(key, value);
        return this;
    }

    /**
     * Return the map.
     * 
     * @return The map.
     */
    public Map<K, V> get() {
        return map;
    }

}
