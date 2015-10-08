package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactHelper;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.ArtifactResolver;
import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;

public class MavenArtifactResolver implements ArtifactResolver {

    @Override
    public <A extends ArtifactDescriptor> A resolve(Coordinates coordinates, Class<A> descriptorType, ScannerContext scannerContext) {
        ArtifactDescriptor artifactDescriptor = find(coordinates, scannerContext);
        if (artifactDescriptor == null) {
            artifactDescriptor = createArtifactDescriptor(coordinates, descriptorType, scannerContext);
        } else if (!(descriptorType.isAssignableFrom(artifactDescriptor.getClass()))) {
            return scannerContext.getStore().migrate(artifactDescriptor, descriptorType);
        }
        return descriptorType.cast(artifactDescriptor);
    }

    @Override
    public ArtifactDescriptor find(Coordinates coordinates, ScannerContext scannerContext) {
        String id = ArtifactHelper.getId(coordinates);
        return scannerContext.getStore().find(ArtifactDescriptor.class, id);
    }

    /**
     * Create an artifact descriptor of a given type.
     * 
     * @param coordinates
     *            The artifact coordinates.
     * @param descriptorType
     *            The descriptor type.
     * @param scannerContext
     *            The scanner context.
     * @param <A>
     *            The descriptor type.
     * @return The artifact descriptor.
     */
    private static <A extends ArtifactDescriptor> A createArtifactDescriptor(Coordinates coordinates, Class<A> descriptorType, ScannerContext scannerContext) {
        A artifactDescriptor = scannerContext.getStore().create(descriptorType, ArtifactHelper.getId(coordinates));
        ArtifactHelper.setCoordinates(artifactDescriptor, coordinates);
        return artifactDescriptor;
    }
}
