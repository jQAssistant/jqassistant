package com.buschmais.jqassistant.plugin.java.api.scanner;

import com.buschmais.jqassistant.plugin.java.api.model.JavaArtifactFileDescriptor;

/**
 * Artifact scoped type resolver which does not consider dependencies.
 */
public class ArtifactScopedTypeResolver extends AbstractArtifactScopedTypeResolver {

    private final String classPathDirectory;

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
    protected String getContainedFileName(String requiredFileName) {
        return classPathDirectory != null ? classPathDirectory + requiredFileName : requiredFileName;
    }
}
