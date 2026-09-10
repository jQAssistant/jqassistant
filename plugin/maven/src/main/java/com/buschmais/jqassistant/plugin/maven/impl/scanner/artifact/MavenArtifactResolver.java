package com.buschmais.jqassistant.plugin.maven.impl.scanner.artifact;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.maven.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven.api.artifact.MavenArtifactHelper;
import com.buschmais.jqassistant.plugin.maven.api.model.MavenArtifactDescriptor;
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
        Map<String, Object> params = new HashMap<>();
        params.put("fqn", fqn);
        Query.Result<CompositeRowObject> result = scannerContext.getStore().executeQuery("MERGE (a:Maven:Artifact{fqn:$fqn}) RETURN a", params);
        return result.hasResult() ? result.getSingleResult().get("a", MavenArtifactDescriptor.class) : null;
    }
}
