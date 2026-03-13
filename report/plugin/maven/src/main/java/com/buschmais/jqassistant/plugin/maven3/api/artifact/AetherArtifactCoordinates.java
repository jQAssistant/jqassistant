package com.buschmais.jqassistant.plugin.maven3.api.artifact;

import org.eclipse.aether.artifact.Artifact;

public class AetherArtifactCoordinates implements Coordinates {

    private Artifact artifact;

    public AetherArtifactCoordinates(Artifact artifact) {
        this.artifact = artifact;
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
        return artifact.getExtension();
    }

    @Override
    public String getVersion() {
        return artifact.getVersion();
    }
}
