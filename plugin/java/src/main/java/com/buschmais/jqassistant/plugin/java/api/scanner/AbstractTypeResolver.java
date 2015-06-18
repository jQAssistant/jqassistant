package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.jqassistant.plugin.java.api.scanner.TypeCache.CachedType;

/**
 * Abstract base implementation of a type resolver.
 */
public abstract class AbstractTypeResolver implements TypeResolver {

    /**
     * The type cache.
     */
    private TypeCache typeCache;

    /**
     * Constructor.
     */
    protected AbstractTypeResolver() {
        this.typeCache = new TypeCache();
    }

    @Override
    public <T extends ClassFileDescriptor> CachedType<T> create(String fullQualifiedName, FileDescriptor fileDescriptor, Class<T> descriptorType,
            ScannerContext context) {
        T typeDescriptor = migrateDescriptor(fullQualifiedName, fileDescriptor, descriptorType, context);
        setTypeProperties(typeDescriptor, fullQualifiedName);
        CachedType<T> cachedType = getCachedType(fullQualifiedName, typeDescriptor);
        addContainedType(fullQualifiedName, cachedType.getTypeDescriptor());
        return cachedType;
    }

    @Override
    public CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context) {
        CachedType<TypeDescriptor> cachedType = typeCache.get(fullQualifiedName);
        if (cachedType == null) {
            TypeDescriptor typeDescriptor = findInArtifact(fullQualifiedName, context);
            if (typeDescriptor == null) {
                typeDescriptor = findInDependencies(fullQualifiedName, context);
            }
            if (typeDescriptor == null) {
                typeDescriptor = createDescriptor(fullQualifiedName, TypeDescriptor.class, context);
                addRequiredType(fullQualifiedName, typeDescriptor);
            }
            cachedType = getCachedType(fullQualifiedName, typeDescriptor);
        }
        return cachedType;
    }

    private <T extends TypeDescriptor> CachedType<T> getCachedType(String fullQualifiedName, TypeDescriptor typeDescriptor) {
        CachedType<T> cachedType = new CachedType(typeDescriptor);
        typeCache.put(fullQualifiedName, cachedType);
        return cachedType;
    }

    private <T extends TypeDescriptor> T createDescriptor(String fullQualifiedName, Class<T> descriptorType, ScannerContext scannerContext) {
        T typeDescriptor = scannerContext.getStore().create(descriptorType);
        setTypeProperties(typeDescriptor, fullQualifiedName);
        return typeDescriptor;
    }

    private <T extends TypeDescriptor> void setTypeProperties(T typeDescriptor, String fullQualifiedName) {
        String name;
        int separatorIndex = fullQualifiedName.lastIndexOf('.');
        if (separatorIndex != -1) {
            name = fullQualifiedName.substring(separatorIndex + 1);
        } else {
            name = fullQualifiedName;
        }
        typeDescriptor.setName(name);
        typeDescriptor.setFullQualifiedName(fullQualifiedName);
    }

    private <T extends TypeDescriptor> T migrateDescriptor(String fqn, Descriptor resolvedType, Class<T> descriptorType, ScannerContext context) {
        T typeDescriptor = context.getStore().migrate(resolvedType, descriptorType);
        removeRequiredType(fqn, typeDescriptor);
        return typeDescriptor;
    }

    /**
     * Find a type descriptor in the current scope (e.g. the containing
     * artifact).
     * 
     * @param fullQualifiedName
     *            The name.
     * @param context
     *            The scanner context.
     * @return The type descriptor.
     */
    protected abstract TypeDescriptor findInArtifact(String fullQualifiedName, ScannerContext context);

    /**
     * Find a type descriptor outside the current scope (e.g. the known
     * dependencies).
     * 
     * @param fullQualifiedName
     *            The name.
     * @param context
     *            The scanner context.
     * @return The type descriptor.
     */
    protected abstract TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context);

    /**
     * Mark a type descriptor as required by the current scope.
     * 
     * @param fqn
     *            The name.
     * @param typeDescriptor
     *            The descriptor.
     */
    protected abstract void addRequiredType(String fqn, TypeDescriptor typeDescriptor);

    /**
     * Mark a type descriptor as contained by the current scope.
     * 
     * @param fqn
     *            The name.
     * @param typeDescriptor
     *            The descriptor.
     */
    protected abstract void addContainedType(String fqn, TypeDescriptor typeDescriptor);

    /**
     * Mark a type descriptor as no longer required by the current scope.
     * 
     * @param fqn
     *            The name.
     * @param typeDescriptor
     *            The descriptor.
     */
    protected abstract <T extends TypeDescriptor> void removeRequiredType(String fqn, T typeDescriptor);
}
