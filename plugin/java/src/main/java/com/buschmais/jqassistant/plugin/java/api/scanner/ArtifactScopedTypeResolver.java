package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;
import com.buschmais.jqassistant.plugin.java.api.model.TypeDescriptor;

/**
 * Artifact scoped type resolver which does not consider dependencies.
 */
public class ArtifactScopedTypeResolver extends AbstractArtifactScopedTypeResolver {

    private String classPathDirectory;

    /**
     * Constructor.
     *
     * @param artifact
     *            The artifact.
     */
    public ArtifactScopedTypeResolver(JavaArtifactFileDescriptor artifact) {
        this(artifact, null);
    }

    /**
     * Constructor.
     *
     * @param artifact
     *            The artifact.
     * @param classPathDirectory
     *            The internal directory, where classes are located, e.g.
     *            "/WEB-INF/classes" (without trailing slash)
     */
    public ArtifactScopedTypeResolver(JavaArtifactFileDescriptor artifact, String classPathDirectory) {
        super(artifact);
        this.classPathDirectory = classPathDirectory;
    }

    @Override
    protected TypeDescriptor findInDependencies(String fullQualifiedName, ScannerContext context) {
        return null;
    }

    @Override
    protected String getContainedFileName(String requiredFileName) {
        return classPathDirectory != null ? classPathDirectory + requiredFileName : requiredFileName;
    }
}
