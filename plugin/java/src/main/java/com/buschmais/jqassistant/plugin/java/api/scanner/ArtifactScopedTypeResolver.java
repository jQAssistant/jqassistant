package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Artifact scoped type resolver which does not consider dependencies.
 */
public class ArtifactScopedTypeResolver extends AbstractArtifactScopedTypeResolver {

    /**
     * Constructor.
     * 
     * @param artifact
     *            The artifact.
     */
    public ArtifactScopedTypeResolver(JavaArtifactFileDescriptor artifact) {
        super(artifact);
    }

    @Override
    protected TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context) {
        return null;
    }

}
