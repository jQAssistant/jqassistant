package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;

import org.eclipse.aether.artifact.DefaultArtifact;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.model.Descriptor;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolverStrategy;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;

/**
 * A file resolver strategy for a local maven repository.
 * 
 * If a file is given which is part of the local maven repository then this
 * strategy will lookup an existing artifact descriptor, i.e.
 */
public class RepositoryFileResolverStrategy implements FileResolverStrategy {

    private static final Logger LOGGER = LoggerFactory.getLogger(RepositoryFileResolverStrategy.class);

    private String repositoryRootPath;

    /**
     * Constructor.
     * 
     * @param repositoryRoot
     *            The root directory of the local repository.
     */
    public RepositoryFileResolverStrategy(File repositoryRoot) {
        this.repositoryRootPath = repositoryRoot.getAbsolutePath().replace('\\', '/');
    }

    @Override
    public Descriptor require(String path, ScannerContext context) {
        return match(path, context);
    }

    @Override
    public Descriptor match(String path, ScannerContext context) {
        if (isArtifactPath(path)) {
            ArtifactCoordinates coordinates = getArtifactCoordinates(path);
            return context.peek(ArtifactResolver.class).find(coordinates, context);
        }
        return null;
    }

    private boolean isArtifactPath(String path) {
        return path.startsWith(repositoryRootPath);
    }

    private ArtifactCoordinates getArtifactCoordinates(String path) {
        LOGGER.debug("Resolving file '{}' within the local Maven repository.", path);
        String localPath = path.substring(repositoryRootPath.length() + 1);
        // the local path in the directory will be used to infer the artifact
        // coordinates
        // 1. groupId
        String[] elements = localPath.split("/");
        StringBuilder groupIdBuilder = new StringBuilder();
        for (int i = 0; i < elements.length - 3; i++) {
            if (groupIdBuilder.length() > 0) {
                groupIdBuilder.append('.');
            }
            groupIdBuilder.append(elements[i]);
        }
        String groupId = groupIdBuilder.toString();
        // 2. artifactId
        String artifactId = elements[elements.length - 3];
        // 3. version
        String version = elements[elements.length - 2];
        // 4. fileName, used to detect an optional classifier
        String fileName = elements[elements.length - 1];
        int typeSeparator = fileName.lastIndexOf(".");
        String type = fileName.substring(typeSeparator + 1);
        String mainArtifactFile = artifactId + "-" + version;
        String classifierPart = fileName.substring(mainArtifactFile.length(), typeSeparator);
        String classifier = classifierPart.isEmpty() ? null : classifierPart.substring(1);
        // lookup artifact
        DefaultArtifact artifact = new DefaultArtifact(groupId, artifactId, classifier, type, version);
        LOGGER.debug("Determined artifact '{}' for file '{}'", artifact, path);
        return new ArtifactCoordinates(artifact);
    }
}
