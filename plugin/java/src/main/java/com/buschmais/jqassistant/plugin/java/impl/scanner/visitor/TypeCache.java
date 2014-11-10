package com.buschmais.jqassistant.plugin.java.impl.scanner.visitor;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.plugin.java.api.model.FieldDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.MethodDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.google.common.cache.*;

/**
 * Cache for resolved types.
 */
public class TypeCache {

    private Cache<String, CachedType> lruCache;
    private Cache<String, CachedType> softCache;

    /**
     * Constructor.
     */
    TypeCache() {
        this.lruCache = CacheBuilder.newBuilder().maximumSize(16384).removalListener(new RemovalListener<String, CachedType>() {
            @Override
            public void onRemoval(RemovalNotification<String, CachedType> notification) {
                if (RemovalCause.SIZE.equals(notification.getCause())) {
                    softCache.put(notification.getKey(), notification.getValue());
                }
            }
        }).build();
        this.softCache = CacheBuilder.newBuilder().softValues().build();
    }

    /**
     * Find a type by its fully qualified named.
     * 
     * @param fullQualifiedName
     *            The fqn.
     * @return The cached type or <code>null</code>.
     */
    public CachedType get(String fullQualifiedName) {
        CachedType cachedType = lruCache.getIfPresent(fullQualifiedName);
        if (cachedType != null) {
            return cachedType;
        }
        cachedType = softCache.getIfPresent(fullQualifiedName);
        if (cachedType != null) {
            lruCache.put(fullQualifiedName, cachedType);
        }
        return cachedType;
    }

    /**
     * Put a type.
     * 
     * @param fullQualifiedName
     *            The fqn.
     * @param cachedType
     *            The type.
     */
    public void put(String fullQualifiedName, CachedType cachedType) {
        lruCache.put(fullQualifiedName, cachedType);
    }

    /**
     * Represents a type and all of its declared members.
     *
     * @param <T>
     *            The descriptor type.
     */
    public static class CachedType<T extends TypeDescriptor> {
        private T typeDescriptor;
        private Map<String, MethodDescriptor> methods = new HashMap<>();
        private Map<String, FieldDescriptor> fields = new HashMap<>();
        private Map<String, TypeDescriptor> dependencies = new HashMap<>();

        /**
         * Constructor.
         *
         * @param typeDescriptor
         *            The type descriptor.
         */
        public CachedType(T typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
        }

        public T getTypeDescriptor() {
            return typeDescriptor;
        }

        public void migrate(T typeDescriptor) {
            this.typeDescriptor = typeDescriptor;
        }

        FieldDescriptor getField(String signature) {
            return fields.get(signature);
        }

        void addField(String signature, FieldDescriptor field) {
            fields.put(signature, field);
        }

        MethodDescriptor getMethod(String signature) {
            return methods.get(signature);
        }

        void addMethod(String signature, MethodDescriptor method) {
            methods.put(signature, method);
        }

        TypeDescriptor getDependency(String fullQualifiedName) {
            return dependencies.get(fullQualifiedName);
        }

        void addDependency(String fullQualifiedName, TypeDescriptor dependency) {
            dependencies.put(fullQualifiedName, dependency);
        }
    }
}
