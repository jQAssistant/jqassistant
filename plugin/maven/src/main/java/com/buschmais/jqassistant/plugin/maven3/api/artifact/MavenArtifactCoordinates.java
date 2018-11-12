package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import org.apache.maven.artifact.Artifact;

public class MavenArtifactCoordinates implements Coordinates {

    /**
     * The artifact type for test jars.
     */
    public static final String ARTIFACTTYPE_TEST_JAR = "test-jar";

    private Artifact artifact;
    private boolean testJar;

    public MavenArtifactCoordinates(Artifact artifact, boolean testJar) {
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
