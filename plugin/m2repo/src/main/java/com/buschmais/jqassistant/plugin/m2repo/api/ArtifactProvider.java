package com.buschmais.jqassistant.plugin.m2repo.api;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;

import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolverStrategy;
import com.buschmais.jqassistant.plugin.m2repo.api.model.MavenRepositoryDescriptor;

/**
 * Defines the interface for an artifact provider
 */
public interface ArtifactProvider {

    /**
     * Return the repository descriptor for the repository.
     * 
     * @return The repository descriptor.
     */
    MavenRepositoryDescriptor getRepositoryDescriptor();

    /**
     * Provide the given artifact as {@link ArtifactResult}.
     * 
     * @param artifact
     *            The artifact.
     * @return The {@link ArtifactResult}.
     * @throws ArtifactResolutionException
     *             If the artifact cannot be resolved.
     */
    ArtifactResult getArtifact(Artifact artifact) throws ArtifactResolutionException;

    /**
     * Return the file resolver strategy to use for resolving artifacts.
     * 
     * @return The file resolver strategy.
     */
    FileResolverStrategy getFileResolverStrategy();
}
