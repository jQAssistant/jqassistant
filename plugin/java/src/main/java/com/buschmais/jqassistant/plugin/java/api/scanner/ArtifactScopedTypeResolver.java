package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.java.api.model.ClassFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.ModuleDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Artifact scoped type resolver.
 */
public class ArtifactScopedTypeResolver implements TypeResolver {

    private final String classPathDirectory;

    private JavaArtifactFileDescriptor artifact;

    private boolean hasDependencies;

    private Map<String, TypeDescriptor> artifactTypes = new HashMap<>();

    /**
     * The type cache.
     */
    private final TypeCache typeCache;

    /**
     * Constructor.
     *
     * @param artifact
     *     The artifact.
     */
    public ArtifactScopedTypeResolver(JavaArtifactFileDescriptor artifact) {
        this(artifact, null);
    }

    /**
     * Constructor.
     */
    public ArtifactScopedTypeResolver(JavaArtifactFileDescriptor artifact, String classPathDirectory) {
        this.classPathDirectory = classPathDirectory;
        this.artifact = artifact;
        this.hasDependencies = artifact.getNumberOfDependencies() > 0;
        this.typeCache = new TypeCache();
        addToCache(artifact.getContains());
        addToCache(artifact.getRequires());
    }

    @Override
    public final <T extends ClassFileDescriptor> TypeCache.CachedType<T> create(String fullQualifiedName, FileDescriptor fileDescriptor,
        Class<T> descriptorType, ScannerContext context) {
        T typeDescriptor = context.getStore()
            .addDescriptorType(fileDescriptor, descriptorType);
        setTypeProperties(typeDescriptor, fullQualifiedName);
        artifactTypes.put(fullQualifiedName, typeDescriptor);
        return getCachedType(fullQualifiedName, typeDescriptor);
    }

    @Override
    public final TypeCache.CachedType<TypeDescriptor> resolve(String fullQualifiedName, ScannerContext context) {
        TypeCache.CachedType<TypeDescriptor> cachedType = typeCache.get(fullQualifiedName);
        if (cachedType == null) {
            TypeDescriptor typeDescriptor = artifactTypes.get(fullQualifiedName);
            if (typeDescriptor == null) {
                typeDescriptor = hasDependencies ? artifact.resolveRequiredType(fullQualifiedName) : null;
            }
            if (typeDescriptor == null) {
                String requiredFileName = "/" + fullQualifiedName.replace(".", "/") + ".class";
                typeDescriptor = require(requiredFileName, ClassFileDescriptor.class, context);
                setTypeProperties(typeDescriptor, fullQualifiedName);
                artifactTypes.put(fullQualifiedName, typeDescriptor);
            }
            cachedType = getCachedType(fullQualifiedName, typeDescriptor);
        }
        return cachedType;
    }

    @Override
    public final <T extends FileDescriptor> T require(String requiredFileName, Class<T> requiredFileType, ScannerContext context) {
        String containedFileName = classPathDirectory != null ? classPathDirectory + requiredFileName : requiredFileName;
        return context.peek(FileResolver.class)
            .require(requiredFileName, containedFileName, requiredFileType, context);
    }

    @Override
    public final ModuleDescriptor requireModule(String moduleName, String version, ScannerContext scannerContext) {
        ModuleDescriptor requiredModule = artifact.findModuleInDependencies(moduleName, version);
        if (requiredModule != null) {
            return requiredModule;
        }
        return artifact.requireModule(moduleName, version);
    }

    private void addToCache(List<FileDescriptor> fileDescriptors) {
        for (FileDescriptor fileDescriptor : fileDescriptors) {
            if (fileDescriptor instanceof TypeDescriptor) {
                TypeDescriptor typeDescriptor = (TypeDescriptor) fileDescriptor;
                artifactTypes.put(typeDescriptor.getFullQualifiedName(), typeDescriptor);
            }
        }
    }

    private <T extends TypeDescriptor> TypeCache.CachedType<T> getCachedType(String fullQualifiedName, TypeDescriptor typeDescriptor) {
        TypeCache.CachedType<T> cachedType = new TypeCache.CachedType(typeDescriptor);
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
}
