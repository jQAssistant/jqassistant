package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import org.apache.commons.lang.StringUtils;

import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;

/**
 * Contains artifact related functionality.
 */
public class MavenArtifactHelper {

    /**
     * Apply the given coordinates to an artifact descriptor.
     *
     * @param artifactDescriptor
     *            The artifact descriptor.
     * @param coordinates
     *            The coordinates.
     */
    public static void setCoordinates(MavenArtifactDescriptor artifactDescriptor, Coordinates coordinates) {
        artifactDescriptor.setGroup(coordinates.getGroup());
        artifactDescriptor.setName(coordinates.getName());
        artifactDescriptor.setVersion(coordinates.getVersion());
        artifactDescriptor.setClassifier(coordinates.getClassifier());
        artifactDescriptor.setType(coordinates.getType());
    }

    /**
     * Set the fully qualified name of an artifact descriptor.
     *
     * @param artifactDescriptor
     *            The artifact descriptor.
     * @param coordinates
     *            The coordinates.
     */
    public static void setId(MavenArtifactDescriptor artifactDescriptor, Coordinates coordinates) {
        artifactDescriptor.setFullQualifiedName(MavenArtifactHelper.getId(coordinates));
    }
    /**
     * Creates the id of an coordinates descriptor by the given items.
     *
     * @param coordinates
     *            The maven coordinates.
     * @return The id.
     */
    public static String getId(Coordinates coordinates) {
        StringBuilder id = new StringBuilder();
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
