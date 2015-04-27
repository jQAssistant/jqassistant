package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Artifact scoped type resolver which considers dependencies, i.e. a classpath.
 */
public class ClasspathScopedTypeResolver extends AbstractArtifactScopedTypeResolver {

    private boolean hasDependencies;

    /**
     * Constructor.
     * 
     * @param artifact
     *            The artifact which defines the scope for resolving types.
     */
    public ClasspathScopedTypeResolver(JavaArtifactFileDescriptor artifact) {
        super(artifact);
        hasDependencies = artifact.getNumberOfDependencies() > 0;
    }

    @Override
    protected TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context) {
        return hasDependencies ? getArtifact().resolveRequiredType(fullQualifiedName) : null;
    }
}
