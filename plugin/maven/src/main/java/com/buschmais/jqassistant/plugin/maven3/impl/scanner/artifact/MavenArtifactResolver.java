package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import java.util.HashMap;
import java.util.Map;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.MavenArtifactHelper;
import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;
import com.buschmais.xo.api.Query;
import com.buschmais.xo.api.Query.Result.CompositeRowObject;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

public class MavenArtifactResolver implements ArtifactResolver {

    private Cache<String, MavenArtifactDescriptor> cache = Caffeine.newBuilder().maximumSize(256).build();

    @Override
    public MavenArtifactDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        String fqn = MavenArtifactHelper.getId(coordinates);
        MavenArtifactDescriptor artifactDescriptor1 = cache.get(fqn, key -> {
            MavenArtifactDescriptor artifactDescriptor = find(key, scannerContext);
            if (artifactDescriptor == null) {
                artifactDescriptor = scannerContext.getStore().create(MavenArtifactDescriptor.class, key);
                MavenArtifactHelper.setCoordinates(artifactDescriptor, coordinates);
            }
            return artifactDescriptor;
        });
        return artifactDescriptor1;
    }

    private MavenArtifactDescriptor find(String fqn, ScannerContext scannerContext) {
        Map<String, Object> params = new HashMap<>();
        params.put("fqn", fqn);
        Query.Result<CompositeRowObject> result = scannerContext.getStore().executeQuery("MATCH (a:Maven:Artifact:File) WHERE a.fqn={fqn} RETURN a", params);
        return result.hasResult() ? result.getSingleResult().get("a", MavenArtifactDescriptor.class) : null;
    }
}
