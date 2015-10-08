package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;

public class ArtifactCoordinates implements Coordinates {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    private org.apache.maven.artifact.Artifact artifact;
    private boolean testJar;

    public ArtifactCoordinates(org.apache.maven.artifact.Artifact artifact) {
        this(artifact, false);
    }

    public ArtifactCoordinates(org.apache.maven.artifact.Artifact artifact, boolean testJar) {
        this.artifact = artifact;
        this.testJar = testJar;
    }

    @Override
    public String getGroup() {
        return artifact.getGroupId();
    }

    @Override
    public String getName() {
        return artifact.getArtifactId();
    }

    @Override
    public String getClassifier() {
        return artifact.getClassifier();
    }

    @Override
    public String getType() {
        return testJar ? ARTIFACTTYPE_TEST_JAR : artifact.getType();
    }

    @Override
    public String getVersion() {
        return artifact.getVersion();
    }
}
