package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import java.io.File;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactFileDescriptor;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class MavenRepositoryArtifactResolver implements ArtifactResolver {

    private static final String CACHE_KEY = MavenRepositoryArtifactResolver.class.getName();

    private final String repositoryRoot;

    private final FileResolver fileResolver;

    /**
     * Constructor.
     *
     * @param repositoryRoot
     *     The root directory of the local repository.
     * @param fileResolver
     *     The {@link FileResolver} to be used for looking up files in the
     *     local repository.
     */
    public MavenRepositoryArtifactResolver(File repositoryRoot, FileResolver fileResolver) {
        this.repositoryRoot = repositoryRoot.getAbsolutePath()
            .replace('\\', '/');
        this.fileResolver = fileResolver;
    }

    @Override
    public MavenArtifactDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        String fqn = MavenArtifactHelper.getId(coordinates);
        return scannerContext.getStore()
            .<String, MavenArtifactDescriptor>getCache(CACHE_KEY)
            .get(fqn, key -> {
                String fileName = getRequiredPath(coordinates);
                MavenArtifactFileDescriptor mavenArtifactDescriptor = fileResolver.require(fileName, MavenArtifactFileDescriptor.class, scannerContext);
                MavenArtifactHelper.setCoordinates(mavenArtifactDescriptor, coordinates);
                return mavenArtifactDescriptor;
            });
    }

    private String getRequiredPath(Coordinates coordinates) {
        String group = coordinates.getGroup();
        String name = coordinates.getName();
        String version = coordinates.getVersion();
        String classifier = coordinates.getClassifier();
        String type = coordinates.getType();
        StringBuilder requiredPath = new StringBuilder(repositoryRoot);
        requiredPath.append(group.replace('.', '/'));
        requiredPath.append('/');
        requiredPath.append(name);
        if (isNotEmpty(version)) {
            requiredPath.append('/');
            requiredPath.append(version);
        }
        requiredPath.append('/');
        requiredPath.append(name);
        if (isNotEmpty(version)) {
            requiredPath.append('-');
            requiredPath.append(version);
        }
        if (isNotEmpty(classifier)) {
            requiredPath.append('-');
            requiredPath.append(classifier);
        }
        requiredPath.append('.');
        requiredPath.append(type);
        return requiredPath.toString();
    }
}
