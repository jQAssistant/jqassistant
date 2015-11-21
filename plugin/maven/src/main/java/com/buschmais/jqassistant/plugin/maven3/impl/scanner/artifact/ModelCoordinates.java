package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import org.apache.maven.model.Model;

import com.buschmais.jqassistant.plugin.maven3.api.artifact.Coordinates;

public final class ModelCoordinates implements Coordinates {

    private Model model;

    public ModelCoordinates(Model model) {
        this.model = model;
    }

    @Override
    public String getGroup() {
        return model.getGroupId();
    }

    @Override
    public String getName() {
        return model.getArtifactId();
    }

    @Override
    public String getClassifier() {
        return null;
    }

    @Override
    public String getType() {
        return model.getPackaging();
    }

    @Override
    public String getVersion() {
        return model.getVersion();
    }

}
