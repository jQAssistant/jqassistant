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

public class MavenArtifactResolver implements ArtifactResolver {

    @Override
    public MavenArtifactDescriptor resolve(Coordinates coordinates, ScannerContext scannerContext) {
        MavenArtifactDescriptor artifactDescriptor = find(coordinates, scannerContext);
        if (artifactDescriptor == null) {
            artifactDescriptor = createArtifactDescriptor(coordinates, scannerContext);
        }
        return artifactDescriptor;
    }

    private MavenArtifactDescriptor find(Coordinates coordinates, ScannerContext scannerContext) {
        String id = MavenArtifactHelper.getId(coordinates);
        Map<String, Object> params = new HashMap<>();
        params.put("fqn", id);
        Query.Result<CompositeRowObject> result = scannerContext.getStore().executeQuery("MATCH (a:Maven:Artifact:File) WHERE a.fqn={fqn} RETURN a", params);
        return result.hasResult() ? result.getSingleResult().get("a", MavenArtifactDescriptor.class) : null;
    }

    /**
     * Create an artifact descriptor of a given type.
     * 
     * @param coordinates
     *            The artifact coordinates.
     * @param scannerContext
     *            The scanner context.
     * @return The artifact descriptor.
     */
    private MavenArtifactDescriptor createArtifactDescriptor(Coordinates coordinates, ScannerContext scannerContext) {
        String id = MavenArtifactHelper.getId(coordinates);
        MavenArtifactDescriptor artifactDescriptor = scannerContext.getStore().create(MavenArtifactDescriptor.class, id);
        MavenArtifactHelper.setCoordinates(artifactDescriptor, coordinates);
        return artifactDescriptor;
    }
}
