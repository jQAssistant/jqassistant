package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactHelper;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

public class MavenArtifactResolver implements ArtifactResolver {

    private static final String CACHE_KEY = MavenArtifactResolver.class.getName();

    @Override
    public MavenArtifactDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        String fqn = MavenArtifactHelper.getId(coordinates);
        return scannerContext.getStore().<String, MavenArtifactDescriptor> getCache(CACHE_KEY).get(fqn, key -> {
            MavenArtifactDescriptor artifactDescriptor = find(key, scannerContext);
            MavenArtifactHelper.setCoordinates(artifactDescriptor, coordinates);
            return artifactDescriptor;
        });
    }

    private MavenArtifactDescriptor find(String fqn, ScannerContext scannerContext) {
        Query.Result<CompositeRowObject> result = scannerContext.getStore().executeQuery("MERGE (a:Maven:Artifact{fqn:$fqn}) RETURN a", Map.of("fqn", fqn));
        return result.hasResult() ? result.getSingleResult().get("a", MavenArtifactDescriptor.class) : null;
    }
}
