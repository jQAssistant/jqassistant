package com.buschmais.jqassistant.core.shared.map;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

import static java.util.Collections.unmodifiableMap;

/**
 * Utility class for building maps using a fluent API.
 *
 * @param <K>
 *     The key type.
 * @param <V>
 *     The value Type.
 * @deprecated Use Map.of instead.
 */
@Deprecated(forRemoval = true)
@ToBeRemovedInVersion(major = 3, minor = 0)
public class MapBuilder<K, V> {

    private final Map<K, V> map = new HashMap<>();

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
     *     The key.
     * @param value
     *     The value.
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
