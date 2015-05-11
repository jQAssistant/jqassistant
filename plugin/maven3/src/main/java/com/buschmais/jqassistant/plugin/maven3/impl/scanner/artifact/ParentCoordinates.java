package com.buschmais.jqassistant.plugin.maven3.impl.scanner.artifact;

import org.apache.maven.model.Parent;

import com.buschmais.jqassistant.plugin.common.api.scanner.artifact.Coordinates;

/**
 * Created by dimahler on 5/5/2015.
 */
public final class ParentCoordinates implements Coordinates {
    private Parent parent;

    public ParentCoordinates(Parent parent) {
        this.parent = parent;
    }

    @Override
    public String getGroup() {
        return parent.getGroupId();
    }

    @Override
    public String getName() {
        return parent.getArtifactId();
    }

    @Override
    public String getClassifier() {
        return null;
    }

    @Override
    public String getType() {
        return "pom";
    }

    @Override
    public String getVersion() {
        return parent.getVersion();
    }

}
