package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.FileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

public abstract class AbstractArtifactScopedTypeResolver extends AbstractTypeResolver {

    private JavaArtifactFileDescriptor artifact;

    private Map<String, TypeDescriptor> artifactTypes = new HashMap<>();

    protected AbstractArtifactScopedTypeResolver(JavaArtifactFileDescriptor artifact) {
        this.artifact = artifact;
        addToCache(artifact.getContains());
        addToCache(artifact.getRequires());
    }

    private void addToCache(List<FileDescriptor> fileDescriptors) {
        for (FileDescriptor fileDescriptor : fileDescriptors) {
            if (fileDescriptor instanceof TypeDescriptor) {
                TypeDescriptor typeDescriptor = (TypeDescriptor) fileDescriptor;
                artifactTypes.put(typeDescriptor.getFullQualifiedName(), typeDescriptor);
            }
        }
    }

    protected JavaArtifactFileDescriptor getArtifact() {
        return artifact;
    }

    @Override
    protected TypeDescriptor findInArtifact(String fullQualifiedName, ScannerContext context) {
        return artifactTypes.get(fullQualifiedName);
    }

    @Override
    protected void addContainedType(String fqn, TypeDescriptor typeDescriptor) {
        artifactTypes.put(fqn, typeDescriptor);
    }

    @Override
    protected void addRequiredType(String fqn, TypeDescriptor typeDescriptor) {
        artifactTypes.put(fqn, typeDescriptor);
    }

    @Override
    protected <T extends TypeDescriptor> void removeRequiredType(String fqn, T typeDescriptor) {
    }
}
