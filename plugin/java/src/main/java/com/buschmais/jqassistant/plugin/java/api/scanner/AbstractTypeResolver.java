package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ModuleDescriptor;
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
    public final <T extends ClassFileDescriptor> CachedType<T> create(String fullQualifiedName, FileDescriptor fileDescriptor, Class<T> descriptorType,
        ScannerContext context) {
        T typeDescriptor = context.getStore()
            .addDescriptorType(fileDescriptor, descriptorType);
        setTypeProperties(typeDescriptor, fullQualifiedName);
        removeRequiredType(fullQualifiedName, typeDescriptor);
        addContainedType(fullQualifiedName, typeDescriptor);
        return getCachedType(fullQualifiedName, typeDescriptor);
    }

    @Override
    public final CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context) {
        CachedType<TypeDescriptor> cachedType = typeCache.get(fullQualifiedName);
        if (cachedType == null) {
            TypeDescriptor typeDescriptor = findInArtifact(fullQualifiedName, context);
            if (typeDescriptor == null) {
                typeDescriptor = findInDependencies(fullQualifiedName, context);
            }
            if (typeDescriptor == null) {
                String requiredFileName = "/" + fullQualifiedName.replace(".", "/") + ".class";
                typeDescriptor = require(requiredFileName, ClassFileDescriptor.class, context);
                setTypeProperties(typeDescriptor, fullQualifiedName);
                addRequiredType(fullQualifiedName, typeDescriptor);
            }
            cachedType = getCachedType(fullQualifiedName, typeDescriptor);
        }
        return cachedType;
    }

    @Override
    public final <T extends FileDescriptor> T require(String requiredFileName, Class<T> requiredFileType, ScannerContext context) {
        String containedFileName = getContainedFileName(requiredFileName);
        return context.peek(FileResolver.class)
            .require(requiredFileName, containedFileName, requiredFileType, context);
    }

    @Override
    public final ModuleDescriptor resolveModule(String moduleName, String version, ScannerContext context) {
        ModuleDescriptor requiredModule = findModuleInDependencies(moduleName, version);
        if (requiredModule == null) {
            requiredModule = context.getStore()
                .create(ModuleDescriptor.class);
            requiredModule.setName(moduleName);
            requiredModule.setVersion(version);
            addRequiredModule(requiredModule);
        }
        return requiredModule;
    }

    private <T extends TypeDescriptor> CachedType<T> getCachedType(String fullQualifiedName, TypeDescriptor typeDescriptor) {
        CachedType<T> cachedType = new CachedType(typeDescriptor);
        typeCache.put(fullQualifiedName, cachedType);
        return cachedType;
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

    /**
     * Find the given module in the dependencies of the current artifact
     *
     * @param moduleName
     *     The module name.
     * @param version
     *     The version.
     * @return The {@link ModuleDescriptor} or <code>null</code>.
     */
    protected abstract ModuleDescriptor findModuleInDependencies(String moduleName, String version);

    protected abstract void addRequiredModule(ModuleDescriptor moduleDescriptor);

    protected abstract String getContainedFileName(String requiredFileName);

    /**
     * Find a type descriptor in the current scope (e.g. the containing
     * artifact).
     *
     * @param fullQualifiedName
     *     The name.
     * @param context
     *     The scanner context.
     * @return The type descriptor.
     */
    protected abstract TypeDescriptor findInArtifact(String fullQualifiedName, ScannerContext context);

    /**
     * Find a type descriptor outside the current scope (e.g. the known
     * dependencies).
     *
     * @param fullQualifiedName
     *     The name.
     * @param context
     *     The scanner context.
     * @return The type descriptor.
     */
    protected abstract TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context);

    /**
     * Mark a type descriptor as required by the current scope.
     *
     * @param fqn
     *     The name.
     * @param typeDescriptor
     *     The descriptor.
     */
    protected abstract void addRequiredType(String fqn, TypeDescriptor typeDescriptor);

    /**
     * Mark a type descriptor as contained by the current scope.
     *
     * @param fqn
     *     The name.
     * @param typeDescriptor
     *     The descriptor.
     */
    protected abstract void addContainedType(String fqn, TypeDescriptor typeDescriptor);

    /**
     * Mark a type descriptor as no longer required by the current scope.
     *
     * @param fqn
     *     The name.
     * @param typeDescriptor
     *     The descriptor.
     */
    protected abstract <T extends TypeDescriptor> void removeRequiredType(String fqn, T typeDescriptor);

}
