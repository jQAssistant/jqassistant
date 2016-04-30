package com.buschmais.jqassistant.plugin.m2repo.impl.scanner;

import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;

import org.eclipse.aether.artifact.Artifact;

public class ArtifactCoordinates implements Coordinates {

    private Artifact artifact;

    public ArtifactCoordinates(Artifact artifact) {
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
