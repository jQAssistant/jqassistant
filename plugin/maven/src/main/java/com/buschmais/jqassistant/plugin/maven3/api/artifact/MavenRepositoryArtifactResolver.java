package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactFileDescriptor;

import org.apache.commons.lang3.StringUtils;

public class MavenRepositoryArtifactResolver implements ArtifactResolver {

    private static final String CACHE_KEY = MavenRepositoryArtifactResolver.class.getName();

    private final String repositoryRoot;

    private final FileResolver fileResolver;

    /**
     * Constructor.
     *
     * @param repositoryRoot
     *            The root directory of the local repository.
     * @param fileResolver
     *            The {@link FileResolver} to be used for looking up files in the
     *            local repository.
     */
    public MavenRepositoryArtifactResolver(File repositoryRoot, FileResolver fileResolver) {
        this.repositoryRoot = repositoryRoot.getAbsolutePath().replace('\\', '/');
        this.fileResolver = fileResolver;
    }

    @Override
    public MavenArtifactDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        String fqn = MavenArtifactHelper.getId(coordinates);
        return scannerContext.getStore().<String, MavenArtifactFileDescriptor> getCache(CACHE_KEY).get(fqn, key -> {
            String fileName = getFileName(coordinates);
            MavenArtifactFileDescriptor mavenArtifactDescriptor = fileResolver.require(fileName, MavenArtifactFileDescriptor.class, scannerContext);
            MavenArtifactHelper.setCoordinates(mavenArtifactDescriptor, coordinates);
            return mavenArtifactDescriptor;
        });
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
