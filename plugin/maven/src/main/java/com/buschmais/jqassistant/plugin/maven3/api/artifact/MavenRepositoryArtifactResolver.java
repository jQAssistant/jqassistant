package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import java.io.File;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.scanner.FileResolver;
import com.buschmais.jqassistant.plugin.common.impl.scanner.PathNormalizer;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactFileDescriptor;
import com.buschmais.xo.api.Query;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

public class MavenRepositoryArtifactResolver implements ArtifactResolver {

    private static final String CACHE_KEY = MavenRepositoryArtifactResolver.class.getName();

    private final String repositoryRootPath;

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
    public MavenRepositoryArtifactResolver(File repositoryRoot, FileResolver fileResolver, ScannerContext context) {
        this.repositoryRootPath = PathNormalizer.normalizeFileName(repositoryRoot, context);
        this.fileResolver = fileResolver;
    }

    @Override
    public MavenArtifactDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        String fqn = MavenArtifactHelper.getId(coordinates);
        return scannerContext.getStore()
            .<String, MavenArtifactDescriptor>getCache(CACHE_KEY)
            .get(fqn, key -> {
                MavenArtifactFileDescriptor mavenArtifactFileDescriptor = findMavenArtifact(scannerContext, fqn);
                if (mavenArtifactFileDescriptor != null) {
                    return mavenArtifactFileDescriptor;
                }
                String fileName = getFileName(coordinates);
                mavenArtifactFileDescriptor = fileResolver.require(fileName, MavenArtifactFileDescriptor.class, scannerContext);
                MavenArtifactHelper.setCoordinates(mavenArtifactFileDescriptor, coordinates);
                return mavenArtifactFileDescriptor;
            });
    }

    private MavenArtifactFileDescriptor findMavenArtifact(ScannerContext scannerContext, String fqn) {
        Query.Result<Query.Result.CompositeRowObject> result = scannerContext.getStore()
            .executeQuery("MATCH (a:Maven:Artifact:File{fqn:$fqn}) RETURN a", Map.of("fqn", fqn));
        return result.hasResult() ?
            result.getSingleResult()
                .get("a", MavenArtifactFileDescriptor.class) :
            null;
    }

    private String getFileName(Coordinates coordinates) {
        String group = coordinates.getGroup();
        String name = coordinates.getName();
        String version = coordinates.getVersion();
        String classifier = coordinates.getClassifier();
        String type = coordinates.getType();
        StringBuilder fileName = new StringBuilder(repositoryRootPath);
        fileName.append('/');
        fileName.append(isNotEmpty(group) ? group.replace('.', '/') : "$");
        fileName.append('/');
        fileName.append(name);
        fileName.append('/');
        fileName.append(isNotEmpty(version) ? version : "$");
        fileName.append('/');
        fileName.append(name);
        fileName.append("-")
            .append(isNotEmpty(version) ? version : "$");
        if (isNotEmpty(classifier)) {
            fileName.append('-');
            fileName.append(classifier);
        }
        fileName.append('.');
        fileName.append(type);
        return fileName.toString();
    }
}
