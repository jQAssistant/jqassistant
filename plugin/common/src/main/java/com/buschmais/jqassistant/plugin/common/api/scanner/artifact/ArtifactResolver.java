package com.buschmais.jqassistant.plugin.common.api.scanner.artifact;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.core.store.api.Store;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;

public class ArtifactResolver {

    /**
     * Resolves an artifact descriptor for the given coordinates, i.e. by looking up an existing one and creating new one on demand.
     * 
     * @param coordinates
     *            The artifact coordinates.
     * @param descriptorType
     *            The required descriptor type.
     * @param scannerContext
     *            The scanner context.
     * @param <A>
     *            The required descriptor type.
     * @return The resolved artifact descriptor.
     */
    public static <A extends ArtifactDescriptor> A resolve(Coordinates coordinates, Class<A> descriptorType, ScannerContext scannerContext) {
        Store store = scannerContext.getStore();
        String id = getArtifactId(coordinates);
        ArtifactDescriptor artifactDescriptor = store.find(ArtifactDescriptor.class, id);
        if (artifactDescriptor == null) {
            artifactDescriptor = createArtifactDescriptor(coordinates, descriptorType, scannerContext);
        } else if (!(descriptorType.isAssignableFrom(artifactDescriptor.getClass()))) {
            return store.migrate(artifactDescriptor, descriptorType);
        }
        return descriptorType.cast(artifactDescriptor);
    }

    /**
     * Apply the given coordinates to an artifact descriptor.
     * 
     * @param artifactDescriptor
     *            The artifact descriptor.
     * @param coordinates
     *            The coordinates.
     * @param <A>
     *            The artifact tpe.
     */
    public static <A extends ArtifactDescriptor> void setCoordinates(A artifactDescriptor, Coordinates coordinates) {
        artifactDescriptor.setFullQualifiedName(getArtifactId(coordinates));
        artifactDescriptor.setGroup(coordinates.getGroup());
        artifactDescriptor.setName(coordinates.getName());
        artifactDescriptor.setVersion(coordinates.getVersion());
        artifactDescriptor.setClassifier(coordinates.getClassifier());
        artifactDescriptor.setType(coordinates.getType());
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
    private static <A extends ArtifactDescriptor> A createArtifactDescriptor(Coordinates coordinates, Class<A> descriptorType,
            ScannerContext scannerContext) {
        String id = getArtifactId(coordinates);
        A artifactDescriptor = scannerContext.getStore().create(descriptorType, id);
        setCoordinates(artifactDescriptor, coordinates);
        artifactDescriptor.setFullQualifiedName(getArtifactId(coordinates));
        return artifactDescriptor;
    }

    /**
     * Creates the id of an coordinates descriptor by the given items.
     *
     * @param coordinates
     *            The maven coordinates.
     * @return The id.
     */
    private static String getArtifactId(Coordinates coordinates) {
        StringBuffer id = new StringBuffer();
        if (coordinates.getGroup() != null) {
            id.append(coordinates.getGroup());
        }
        id.append(':');
        id.append(coordinates.getName());
        id.append(':');
        id.append(coordinates.getType());
        String classifier = coordinates.getClassifier();
        if (classifier != null) {
            id.append(':');
            id.append(classifier);
        }
        id.append(':');
        if (coordinates.getVersion() != null) {
            id.append(coordinates.getVersion());
        }
        return id.toString();
    }
}
