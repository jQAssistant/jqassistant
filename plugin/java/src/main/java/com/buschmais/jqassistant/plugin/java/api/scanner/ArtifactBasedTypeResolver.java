package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.ResultIterator;

/**
 * A type resolver considering an artifact and its optional dependencies as
 * scopes.
 */
class ArtifactBasedTypeResolver extends AbstractTypeResolver {

    private JavaArtifactDescriptor artifact;

    private List<ArtifactFileDescriptor> dependencies;

    private Map<String, TypeDescriptor> artifactTypes = new HashMap<>();

    /**
     * Constructor.
     * 
     * @param artifact
     *            The artifact which defines the scope for resolving types.
     */
    ArtifactBasedTypeResolver(JavaArtifactDescriptor artifact) {
        this.artifact = artifact;
        for (FileDescriptor fileDescriptor : artifact.getContains()) {
            if (fileDescriptor instanceof TypeDescriptor) {
                TypeDescriptor typeDescriptor = (TypeDescriptor) fileDescriptor;
                artifactTypes.put(typeDescriptor.getFullQualifiedName(), typeDescriptor);
            }
        }
        for (TypeDescriptor typeDescriptor : artifact.getRequiresTypes()) {
            this.artifactTypes.put(typeDescriptor.getFullQualifiedName(), typeDescriptor);
        }
        this.dependencies = new ArrayList<>();
        for (DependsOnDescriptor dependsOnDescriptor : artifact.getDependencies()) {
            dependencies.add((ArtifactFileDescriptor) dependsOnDescriptor.getDependency());
        }
    }

    @Override
    protected TypeDescriptor findInArtifact(String fullQualifiedName, ScannerContext context) {
        return artifactTypes.get(fullQualifiedName);
    }

    @Override
    protected TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context) {
        TypeDescriptor typeDescriptor = null;
        if (!dependencies.isEmpty()) {
            Query.Result<TypeDescriptor> typeDescriptors = artifact.resolveRequiredType(fullQualifiedName, dependencies);
            ResultIterator<TypeDescriptor> iterator = typeDescriptors.iterator();
            if (iterator.hasNext()) {
                typeDescriptor = iterator.next();
            }
        }
        return typeDescriptor;
    }

    @Override
    protected void addContainedType(String fqn, TypeDescriptor typeDescriptor) {
        artifactTypes.put(fqn, typeDescriptor);
    }

    @Override
    protected void addRequiredType(String fqn, TypeDescriptor typeDescriptor) {
        artifactTypes.put(fqn, typeDescriptor);
        typeDescriptor.setRequiredBy(artifact);
    }

    @Override
    protected <T extends TypeDescriptor> void removeRequiredType(String fqn, T typeDescriptor) {
        typeDescriptor.setRequiredBy(null);
    }
}
