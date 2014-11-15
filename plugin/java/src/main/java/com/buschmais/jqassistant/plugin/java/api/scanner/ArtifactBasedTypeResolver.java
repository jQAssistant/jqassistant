package com.buschmais.jqassistant.plugin.java.api.scanner;

import java.util.ArrayList;
import java.util.List;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.common.api.model.DependsOnDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

class ArtifactBasedTypeResolver extends AbstractTypeResolver {

    private JavaArtifactDescriptor artifact;

    private List<ArtifactDescriptor> dependencies;

    /**
     * Constructor.
     * 
     * @param artifact
     *            The artifact which defines the scope for resolving types.
     */
    ArtifactBasedTypeResolver(JavaArtifactDescriptor artifact) {
        this.artifact = artifact;
        this.dependencies = new ArrayList<>();
        for (DependsOnDescriptor dependsOnDescriptor : artifact.getDependencies()) {
            dependencies.add(dependsOnDescriptor.getDependency());
        }
    }

    @Override
    protected TypeDescriptor findType(String fullQualifiedName, ScannerContext context) {
        return artifact.resolveType(fullQualifiedName, dependencies);
    }

    @Override
    protected void addRequiredType(TypeDescriptor typeDescriptor) {
        typeDescriptor.setRequiredBy(artifact);
    }

    @Override
    protected <T extends TypeDescriptor> void removeRequiredType(T typeDescriptor) {
        typeDescriptor.setRequiredBy(null);
    }
}
