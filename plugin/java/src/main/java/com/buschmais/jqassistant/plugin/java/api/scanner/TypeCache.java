package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.java.api.model.*;
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
        this.lruCache = CacheBuilder.newBuilder().maximumSize(8192).removalListener(new RemovalListener<String, CachedType>() {
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
        private Map<String, MemberDescriptor> members = null;
        private Map<TypeDescriptor, Integer> dependencies = null;

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

        public FieldDescriptor getField(String signature) {
            return (FieldDescriptor) getMembers().get(signature);
        }

        public MethodDescriptor getMethod(String signature) {
            return (MethodDescriptor) getMembers().get(signature);
        }

        public void addMember(String signature, MemberDescriptor member) {
            typeDescriptor.getDeclaredMembers().add(member);
            getMembers().put(signature, member);
        }

        public void addDependency(TypeDescriptor dependency) {
            Map<TypeDescriptor, Integer> dependencies = getDependencies();
            Integer weight = dependencies.get(dependency);
            if (weight == null) {
                weight = 0;
            }
            weight++;
            getDependencies().put(dependency, weight);
        }

        private Map<String, MemberDescriptor> getMembers() {
            if (members == null) {
                members = new HashMap<>();
                for (Descriptor descriptor : typeDescriptor.getDeclaredMembers()) {
                    if (descriptor instanceof MemberDescriptor) {
                        MemberDescriptor memberDescriptor = (MemberDescriptor) descriptor;
                        members.put(memberDescriptor.getSignature(), memberDescriptor);
                    }
                }
            }
            return members;
        }

        public Map<TypeDescriptor, Integer> getDependencies() {
            if (dependencies == null) {
                dependencies = new HashMap<>();
                for (TypeDependsOnDescriptor dependency : typeDescriptor.getDependencies()) {
                    dependencies.put(dependency.getDependency(), dependency.getWeight());
                }
            }
            return dependencies;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof CachedType))
                return false;
            CachedType that = (CachedType) o;
            if (!typeDescriptor.equals(that.typeDescriptor))
                return false;
            return true;
        }

        @Override
        public int hashCode() {
            return typeDescriptor.hashCode();
        }
    }
}
