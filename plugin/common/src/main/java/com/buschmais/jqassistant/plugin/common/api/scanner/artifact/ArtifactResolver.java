package com.buschmais.jqassistant.plugin.common.api.scanner.artifact;

import org.apache.commons.lang.StringUtils;

import com.buschmais.jqassistant.core.scanner.api.ScannerContext;
import com.buschmais.jqassistant.plugin.common.api.model.ArtifactDescriptor;

public class ArtifactResolver {

    /**
     * Resolves an artifact descriptor for the given coordinates, i.e. by
     * looking up an existing one and creating new one on demand.
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
        ArtifactDescriptor artifactDescriptor = find(coordinates, scannerContext);
        if (artifactDescriptor == null) {
            artifactDescriptor = createArtifactDescriptor(coordinates, descriptorType, scannerContext);
        } else if (!(descriptorType.isAssignableFrom(artifactDescriptor.getClass()))) {
            return scannerContext.getStore().migrate(artifactDescriptor, descriptorType);
        }
        return descriptorType.cast(artifactDescriptor);
    }

    /**
     * Find an artifact identified by the given coordinates.
     * 
     * @param coordinates
     *            The coordinates.
     * @param scannerContext
     *            The scanner context.
     * @return The artifact descriptor or <code>null</code>.
     */
    public static ArtifactDescriptor find(Coordinates coordinates, ScannerContext scannerContext) {
        String id = getId(coordinates);
        return scannerContext.getStore().find(ArtifactDescriptor.class, id);
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
        artifactDescriptor.setFullQualifiedName(getId(coordinates));
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
    private static <A extends ArtifactDescriptor> A createArtifactDescriptor(Coordinates coordinates, Class<A> descriptorType, ScannerContext scannerContext) {
        String id = getId(coordinates);
        A artifactDescriptor = scannerContext.getStore().create(descriptorType, id);
        setCoordinates(artifactDescriptor, coordinates);
        artifactDescriptor.setFullQualifiedName(getId(coordinates));
        return artifactDescriptor;
    }

    /**
     * Creates the id of an coordinates descriptor by the given items.
     *
     * @param coordinates
     *            The maven coordinates.
     * @return The id.
     */
    public static String getId(Coordinates coordinates) {
        StringBuffer id = new StringBuffer();
        if (StringUtils.isNotEmpty(coordinates.getGroup())) {
            id.append(coordinates.getGroup());
        }
        id.append(':');
        id.append(coordinates.getName());
        id.append(':');
        id.append(coordinates.getType());
        String classifier = coordinates.getClassifier();
        if (StringUtils.isNotEmpty(classifier)) {
            id.append(':');
            id.append(classifier);
        }
        String version = coordinates.getVersion();
        if (StringUtils.isNotEmpty(version)) {
            id.append(':');
            id.append(version);
        }
        return id.toString();
    }
}
