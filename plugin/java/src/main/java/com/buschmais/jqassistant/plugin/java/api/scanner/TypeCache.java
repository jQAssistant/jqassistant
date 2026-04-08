package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.RemovalCause;
import com.github.benmanes.caffeine.cache.RemovalListener;

/**
 * Cache for resolved types.
 */
public class TypeCache {

    private Cache<String, TypeDescriptor> lruCache;
    private Cache<String, TypeDescriptor> softCache;

    /**
     * Constructor.
     */
    TypeCache() {
        this.lruCache = Caffeine.newBuilder()
            .maximumSize(8192)
            .removalListener((RemovalListener<String, TypeDescriptor>) (key, value, cause) -> {
                if (RemovalCause.SIZE.equals(cause)) {
                    softCache.put(key, value);
                }
            })
            .build();
        this.softCache = Caffeine.newBuilder()
            .softValues()
            .build();
    }

    /**
     * Find a type by its fully qualified named.
     *
     * @param fullQualifiedName
     *     The fqn.
     * @return The cached type or <code>null</code>.
     */
    public TypeDescriptor get(String fullQualifiedName) {
        TypeDescriptor typeDescriptor = lruCache.getIfPresent(fullQualifiedName);
        if (typeDescriptor != null) {
            return typeDescriptor;
        }
        typeDescriptor = softCache.getIfPresent(fullQualifiedName);
        if (typeDescriptor != null) {
            lruCache.put(fullQualifiedName, typeDescriptor);
        }
        return typeDescriptor;
    }

    /**
     * Put a type.
     *
     * @param fullQualifiedName
     *     The fqn.
     * @param typeDescriptor
     *     The type.
     */
    public void put(String fullQualifiedName, TypeDescriptor typeDescriptor) {
        lruCache.put(fullQualifiedName, typeDescriptor);
    }
}
