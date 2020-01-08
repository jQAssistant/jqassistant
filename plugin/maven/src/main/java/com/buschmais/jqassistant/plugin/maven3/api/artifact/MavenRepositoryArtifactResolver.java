package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactFileDescriptor;

import org.apache.commons.lang3.StringUtils;

public class MavenRepositoryArtifactResolver implements ArtifactResolver {

    private String repositoryRoot;

    /**
     * Constructor.
     *
     * @param repositoryRoot
     *            The root directory of the local repository.
     */
    public MavenRepositoryArtifactResolver(File repositoryRoot) {
        this.repositoryRoot = repositoryRoot.getAbsolutePath().replace('\\', '/');
    }

    @Override
    public MavenArtifactFileDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        String fileName = getFileName(coordinates);
        MavenArtifactFileDescriptor mavenArtifactDescriptor = scannerContext.peek(FileResolver.class).require(fileName, MavenArtifactFileDescriptor.class,
                scannerContext);
        // TODO do not set coordinates on existing descriptors
        MavenArtifactHelper.setCoordinates(mavenArtifactDescriptor, coordinates);
        MavenArtifactHelper.setId(mavenArtifactDescriptor, coordinates);
        return mavenArtifactDescriptor;
    }

    private String getFileName(Coordinates coordinates) {
        String group = coordinates.getGroup();
        String name = coordinates.getName();
        String version = coordinates.getVersion();
        String classifier = coordinates.getClassifier();
        String type = coordinates.getType();
        StringBuilder fileName = new StringBuilder(repositoryRoot);
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
