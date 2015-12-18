package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import java.io.File;

import org.apache.commons.lang.StringUtils;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactHelper;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;

public class MavenRepositoryArtifactResolver implements ArtifactResolver {

    private MavenRepositoryFileResolver repositoryFileResolver;

    private String repositoryRoot;

    /**
     * Constructor.
     * 
     * @param repositoryRoot
     *            The root directory of the local repository.
     * @param repositoryFileResolver
     *            The repository file resolver.
     */
    public MavenRepositoryArtifactResolver(File repositoryRoot, MavenRepositoryFileResolver repositoryFileResolver) {
        this.repositoryFileResolver = repositoryFileResolver;
        this.repositoryRoot = repositoryRoot.getAbsolutePath().replace('\\', '/');
    }

    @Override
    public MavenArtifactDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        String fileName = getFileName(coordinates);
        MavenArtifactDescriptor mavenArtifactDescriptor = repositoryFileResolver.require(fileName, MavenArtifactDescriptor.class, scannerContext);
        // TODO do not set coordinates on existing descriptors
        MavenArtifactHelper.setCoordinates(mavenArtifactDescriptor, coordinates);
        return mavenArtifactDescriptor;
    }

    private String getFileName(Coordinates coordinates) {
        String group = coordinates.getGroup();
        String name = coordinates.getName();
        String version = coordinates.getVersion();
        String classifier = coordinates.getClassifier();
        String type = coordinates.getType();
        StringBuilder fileName = new StringBuilder();
        fileName.append(repositoryRoot);
        if (StringUtils.isNotEmpty(group)) {
            fileName.append('/');
            fileName.append(group.replace('.', '/'));
        }
        fileName.append('/');
        fileName.append(name);
        if (StringUtils.isNotEmpty(version)) {
            fileName.append('/');
            fileName.append(version);
        }
        fileName.append('/');
        fileName.append(name);
        if (StringUtils.isNotEmpty(version)) {
            fileName.append('-');
            fileName.append(version);
        }
        if (StringUtils.isNotEmpty(classifier)) {
            fileName.append('-');
            fileName.append(classifier);
        }
        fileName.append('.');
        fileName.append(type);
        return fileName.toString();
    }
}
