package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.buschmais.jqassistant.plugin.maven3.api.model.MavenArtifactDescriptor;

import org.apache.commons.lang3.StringUtils;

/**
 * Contains artifact related functionality.
 */
public final class MavenArtifactHelper {

    private static final String SNAPSHOT = "SNAPSHOT";
    private static final Pattern SNAPSHOT_TIMESTAMP = Pattern.compile("^(.*-)?([0-9]{8}\\.[0-9]{6}-[0-9]+)$");

    private MavenArtifactHelper() {
    }

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

    /**
     * Determines if the given {@link Coordinates} represent a snapshot.
     *
     * @param coordinates
     *            The coordinates.
     * @return <code>true</code> if the {@link Coordinates} represent a snapshot.
     */
    public static boolean isSnapshot(Coordinates coordinates) {
        String version = coordinates.getVersion();
        return version.endsWith(SNAPSHOT) || SNAPSHOT_TIMESTAMP.matcher(version).matches();
    }

    /**
     * Determine the base version of given {@link Coordinates}.
     *
     * For releases the base version is the given version. If a snapshot version
     * contains a repository timestamp suffix then it will be replaced by
     * {@link #SNAPSHOT}.
     *
     * @param coordinates
     *            The coordinates.
     * @return The base version.
     */
    public static String getBaseVersion(Coordinates coordinates) {
        String version = coordinates.getVersion();
        Matcher m = SNAPSHOT_TIMESTAMP.matcher(version);
        if (m.matches()) {
            if (m.group(1) != null) {
                return m.group(1) + SNAPSHOT;
            } else {
                return SNAPSHOT;
            }
        }
        return version;

    }
}
